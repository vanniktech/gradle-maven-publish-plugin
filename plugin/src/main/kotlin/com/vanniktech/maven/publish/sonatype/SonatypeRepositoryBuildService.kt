package com.vanniktech.maven.publish.sonatype

import com.vanniktech.maven.publish.BuildConfig
import com.vanniktech.maven.publish.central.EndOfBuildAction
import com.vanniktech.maven.publish.central.MavenCentralCoordinates
import com.vanniktech.maven.publish.central.MavenCentralProject
import com.vanniktech.maven.publish.portal.SonatypeCentralPortal
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Base64
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.build.event.BuildEventsListenerRegistry
import org.gradle.tooling.events.FailureResult
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener

internal abstract class SonatypeRepositoryBuildService :
  BuildService<SonatypeRepositoryBuildService.Params>,
  AutoCloseable,
  OperationCompletionListener {
  internal interface Params : BuildServiceParameters {
    val repositoryUsername: Property<String>
    val repositoryPassword: Property<String>
    val okhttpTimeoutSeconds: Property<Long>
    val closeTimeoutSeconds: Property<Long>
  }

  private val centralPortal by lazy {
    SonatypeCentralPortal(
      baseUrl = "https://central.sonatype.com",
      usertoken = Base64
        .getEncoder()
        .encode(
          "${parameters.repositoryUsername.get()}:${parameters.repositoryPassword.get()}".toByteArray(),
        ).toString(Charsets.UTF_8),
      userAgentName = BuildConfig.NAME,
      userAgentVersion = BuildConfig.VERSION,
      okhttpTimeoutSeconds = parameters.okhttpTimeoutSeconds.get(),
      closeTimeoutSeconds = parameters.closeTimeoutSeconds.get(),
    )
  }

  private var publishId: String? = null
    set(value) {
      check(field == null || field == value) {
        "publishId was already set to '$field', new value '$value'"
      }
      field = value
    }

  private val endOfBuildActions = mutableSetOf<EndOfBuildAction>()

  private val projectsToPublish = mutableSetOf<MavenCentralProject>()

  private var buildIsSuccess: Boolean = true

  /**
   * Is only be allowed to be called from task actions.
   */
  fun registerProject(group: String, artifactId: String, version: String, localRepository: File) {
    if (version.endsWith("-SNAPSHOT")) {
      return
    }

    val coordinates = MavenCentralCoordinates(group, artifactId, version)
    val project = MavenCentralProject(coordinates, localRepository)
    projectsToPublish.add(project)

    endOfBuildActions += EndOfBuildAction.Upload
    endOfBuildActions += EndOfBuildAction.Drop(runAfterFailure = true)
  }

  /**
   * Is only be allowed to be called from task actions.
   */
  fun enableAutomaticPublishing() {
    endOfBuildActions += EndOfBuildAction.Publish
  }

  /**
   * Is only be allowed to be called from task actions. Tasks calling this must run after tasks
   * that call [registerProject].
   */
  fun shouldDropRepository(manualStagingRepositoryId: String?) {
    if (manualStagingRepositoryId != null) {
      publishId = manualStagingRepositoryId
    } else {
      error("A deployment id needs to be provided with `--repository` when publishing through Central Portal")
    }

    endOfBuildActions += EndOfBuildAction.Drop(runAfterFailure = false)
  }

  override fun onFinish(event: FinishEvent) {
    if (event.result is FailureResult) {
      buildIsSuccess = false
    }
  }

  override fun close() {
    if (buildIsSuccess) {
      runEndOfBuildActions(endOfBuildActions.filter { !it.runAfterFailure })
    } else {
      // surround with try catch since failing again on clean up actions causes confusion
      try {
        runEndOfBuildActions(endOfBuildActions.filter { it.runAfterFailure })
      } catch (_: IOException) {
      }
    }
  }

  private fun runEndOfBuildActions(actions: List<EndOfBuildAction>) {
    if (actions.contains(EndOfBuildAction.Upload)) {
      val coordinates = projectsToPublish.map { it.coordinates }.toSet()
      val deploymentName = if (coordinates.size == 1) {
        val coordinate = coordinates.single()
        "${coordinate.group}-${coordinate.artifactId}-${coordinate.version}"
      } else if (coordinates.distinctBy { it.group + it.version }.size == 1) {
        val coordinate = coordinates.first()
        "${coordinate.group}-${coordinate.version}"
      } else {
        val coordinate = coordinates.first()
        "${coordinate.group}-${System.currentTimeMillis()}"
      }

      val publishingType = if (actions.contains(EndOfBuildAction.Publish)) {
        "AUTOMATIC"
      } else {
        "USER_MANAGED"
      }

      val zipFile = File.createTempFile("$deploymentName-${System.currentTimeMillis()}", "zip")
      val out = ZipOutputStream(FileOutputStream(zipFile))
      projectsToPublish.forEach { project ->
        project.localRepository.walkTopDown().forEach {
          if (it.isDirectory) {
            return@forEach
          }
          if (it.name.contains("maven-metadata")) {
            return@forEach
          }

          val entry = ZipEntry(it.toRelativeString(project.localRepository))
          out.putNextEntry(entry)
          out.write(it.readBytes())
          out.closeEntry()
        }
      }
      out.close()

      publishId = centralPortal.upload(deploymentName, publishingType, zipFile)
    }

    val dropAction = actions.filterIsInstance<EndOfBuildAction.Drop>().singleOrNull()
    if (dropAction != null) {
      val publishId = publishId
      if (publishId != null) {
        centralPortal.deleteDeployment(publishId)
      }
    }
  }

  companion object {
    private const val NAME = "sonatype-repository-build-service"

    fun Project.registerSonatypeRepositoryBuildService(
      repositoryUsername: Provider<String>,
      repositoryPassword: Provider<String>,
      buildEventsListenerRegistry: BuildEventsListenerRegistry,
    ): Provider<SonatypeRepositoryBuildService> {
      val okhttpTimeout = project.providers
        .gradleProperty("SONATYPE_CONNECT_TIMEOUT_SECONDS")
        .map { it.toLong() }
        .orElse(60)
      val closeTimeout = project.providers
        .gradleProperty("SONATYPE_CLOSE_TIMEOUT_SECONDS")
        .map { it.toLong() }
        .orElse(60 * 15)
      val service = gradle.sharedServices.registerIfAbsent(NAME, SonatypeRepositoryBuildService::class.java) {
        it.maxParallelUsages.set(1)
        it.parameters.repositoryUsername.set(repositoryUsername)
        it.parameters.repositoryPassword.set(repositoryPassword)
        it.parameters.okhttpTimeoutSeconds.set(okhttpTimeout)
        it.parameters.closeTimeoutSeconds.set(closeTimeout)
      }
      buildEventsListenerRegistry.onTaskCompletion(service)
      return service
    }
  }
}
