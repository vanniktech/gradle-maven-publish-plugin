package com.vanniktech.maven.publish.central

import com.vanniktech.maven.publish.BuildConfig
import com.vanniktech.maven.publish.portal.SonatypeCentralPortal
import com.vanniktech.maven.publish.portal.SonatypeCentralPortal.PublishingType.AUTOMATIC
import com.vanniktech.maven.publish.portal.SonatypeCentralPortal.PublishingType.USER_MANAGED
import java.io.File
import java.io.IOException
import java.util.Base64
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.time.Duration.Companion.seconds
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.build.event.BuildEventsListenerRegistry
import org.gradle.tooling.events.FailureResult
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener

internal abstract class MavenCentralBuildService :
  BuildService<MavenCentralBuildService.Params>,
  AutoCloseable,
  OperationCompletionListener {
  internal interface Params : BuildServiceParameters {
    val repositoryUsername: Property<String>
    val repositoryPassword: Property<String>
    val okhttpTimeoutSeconds: Property<Long>
    val rootBuildDirectory: DirectoryProperty
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
      okhttpTimeout = parameters.okhttpTimeoutSeconds.get().seconds,
    )
  }

  private var deploymentId: String? = null

  private val endOfBuildActions = mutableSetOf<EndOfBuildAction>()

  private val projectsToPublish = mutableSetOf<MavenCentralProject>()

  private var buildIsSuccess: Boolean = true

  /**
   * Is only allowed to be called from task actions.
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
   * Is only allowed to be called from task actions.
   */
  fun enableAutomaticPublishing() {
    endOfBuildActions += EndOfBuildAction.Publish
  }

  /**
   * Is only allowed to be called from task actions. Tasks calling this must run after tasks
   * that call [registerProject].
   */
  fun dropDeployment(deploymentId: String) {
    this.deploymentId = deploymentId

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
      // surround with try catch since failing again on cleanup actions causes confusion
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
        AUTOMATIC
      } else {
        USER_MANAGED
      }

      val zipFile = parameters.rootBuildDirectory
        .file("publish/$deploymentName-${System.currentTimeMillis()}.zip")
        .get()
        .asFile
      zipFile.parentFile.mkdirs()
      check(zipFile.createNewFile()) { "$zipFile already exists" }
      val out = ZipOutputStream(zipFile.outputStream())
      projectsToPublish.forEach { project ->
        project.localRepository
          .walkTopDown()
          .filter { it.isFile && !it.name.contains("maven-metadata") }
          .forEach {
            val entry = ZipEntry(it.toRelativeString(project.localRepository))
            out.putNextEntry(entry)
            out.write(it.readBytes())
            out.closeEntry()
          }
      }
      out.close()

      deploymentId = centralPortal.upload(deploymentName, publishingType, zipFile)
    }

    val dropAction = actions.filterIsInstance<EndOfBuildAction.Drop>().singleOrNull()
    if (dropAction != null) {
      val id = deploymentId
      if (id != null) {
        centralPortal.deleteDeployment(id)
      }
    }
  }

  companion object {
    private const val NAME = "maven-central-build-service"

    fun Project.registerMavenCentralBuildService(
      repositoryUsername: Provider<String>,
      repositoryPassword: Provider<String>,
      rootBuildDirectory: Directory,
      buildEventsListenerRegistry: BuildEventsListenerRegistry,
    ): Provider<MavenCentralBuildService> {
      val okhttpTimeout = project.providers
        .gradleProperty("SONATYPE_CONNECT_TIMEOUT_SECONDS")
        .map { it.toLong() }
        .orElse(60)
      val service = gradle.sharedServices.registerIfAbsent(NAME, MavenCentralBuildService::class.java) {
        it.maxParallelUsages.set(1)
        it.parameters.repositoryUsername.set(repositoryUsername)
        it.parameters.repositoryPassword.set(repositoryPassword)
        it.parameters.okhttpTimeoutSeconds.set(okhttpTimeout)
        it.parameters.rootBuildDirectory.set(rootBuildDirectory)
      }
      buildEventsListenerRegistry.onTaskCompletion(service)
      return service
    }
  }
}
