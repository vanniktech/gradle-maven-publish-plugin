package com.vanniktech.maven.publish.sonatype

import com.vanniktech.maven.publish.BuildConfig
import com.vanniktech.maven.publish.central.EndOfBuildAction
import com.vanniktech.maven.publish.central.MavenCentralCoordinates
import com.vanniktech.maven.publish.central.MavenCentralProject
import com.vanniktech.maven.publish.portal.SonatypeCentralPortal
import com.vanniktech.maven.publish.portal.SonatypeCentralPortal.PublishingType.AUTOMATIC
import com.vanniktech.maven.publish.portal.SonatypeCentralPortal.PublishingType.USER_MANAGED
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.Base64
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
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
  private val logger: Logger = Logging.getLogger(SonatypeRepositoryBuildService::class.java)

  internal interface Params : BuildServiceParameters {
    val versionIsSnapshot: Property<Boolean>
    val repositoryUsername: Property<String>
    val repositoryPassword: Property<String>
    val okhttpTimeoutSeconds: Property<Long>
    val closeTimeoutSeconds: Property<Long>
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
      okhttpTimeoutSeconds = parameters.okhttpTimeoutSeconds.get(),
      closeTimeoutSeconds = parameters.closeTimeoutSeconds.get(),
    )
  }

  // used for the publishing tasks
  private var uploadId: String? = null
    set(value) {
      check(field == null || field == value) {
        "uploadId was already set to '$field', new value '$value'"
      }
      field = value
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
  fun registerProject(group: String, artifactId: String, version: String) {
    val coordinates = MavenCentralCoordinates(group, artifactId, version)
    val project = MavenCentralProject(coordinates)
    projectsToPublish.add(project)

    if (parameters.versionIsSnapshot.get()) {
      return
    }

    if (uploadId != null) {
      return
    }

    uploadId = UUID.randomUUID().toString()

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

  internal fun publishingUrl(): URI = if (parameters.versionIsSnapshot.get()) {
    error { "Staging repositories are not supported for SNAPSHOT versions." }
  } else {
    val id = requireNotNull(uploadId) {
      "The staging repository was not created yet. Please open a bug with a build scan or build logs and stacktrace"
    }

    parameters
      .rootBuildDirectory
      .get()
      .asFile
      .resolve("publish/staging/$id")
      .toURI()
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
      } catch (e: IOException) {
        if (buildIsSuccess) {
          throw e
        } else {
          logger.info("Failed processing $uploadId staging repository after previous build failure", e)
        }
      }
    }
  }

  private fun runEndOfBuildActions(actions: List<EndOfBuildAction>) {
    val uploadId = uploadId

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
        "${coordinate.group}-$uploadId"
      }

      val publishingType = if (actions.contains(EndOfBuildAction.Publish)) {
        AUTOMATIC
      } else {
        USER_MANAGED
      }

      val directory = File(publishingUrl())
      val zipFile = File("${directory.absolutePath}.zip")
      val out = ZipOutputStream(zipFile.outputStream())
      directory
        .walkTopDown()
        .filter { it.isFile && !it.name.contains("maven-metadata") }
        .forEach {
          val entry = ZipEntry(it.toRelativeString(directory))
          out.putNextEntry(entry)
          out.write(it.readBytes())
          out.closeEntry()
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
      versionIsSnapshot: Provider<Boolean>,
      repositoryUsername: Provider<String>,
      repositoryPassword: Provider<String>,
      rootBuildDirectory: Provider<Directory>,
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
        it.parameters.versionIsSnapshot.set(versionIsSnapshot)
        it.parameters.repositoryUsername.set(repositoryUsername)
        it.parameters.repositoryPassword.set(repositoryPassword)
        it.parameters.okhttpTimeoutSeconds.set(okhttpTimeout)
        it.parameters.closeTimeoutSeconds.set(closeTimeout)
        it.parameters.rootBuildDirectory.set(rootBuildDirectory)
      }
      buildEventsListenerRegistry.onTaskCompletion(service)
      return service
    }
  }
}
