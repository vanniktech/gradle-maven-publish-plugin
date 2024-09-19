package com.vanniktech.maven.publish.sonatype

import com.vanniktech.maven.publish.BuildConfig
import com.vanniktech.maven.publish.SonatypeHost
import com.vanniktech.maven.publish.nexus.Nexus
import com.vanniktech.maven.publish.portal.SonatypeCentralPortal
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Base64
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.configuration.BuildFeatures
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
  BuildService<SonatypeRepositoryBuildService.Params>, AutoCloseable, OperationCompletionListener {
  private val logger: Logger = Logging.getLogger(SonatypeRepositoryBuildService::class.java)

  @Suppress("UnstableApiUsage")
  @get:Inject
  internal abstract val buildFeatures: BuildFeatures

  internal interface Params : BuildServiceParameters {
    val sonatypeHost: Property<SonatypeHost>
    val groupId: Property<String>
    val versionIsSnapshot: Property<Boolean>
    val repositoryUsername: Property<String>
    val repositoryPassword: Property<String>
    val automaticRelease: Property<Boolean>
    val okhttpTimeoutSeconds: Property<Long>
    val closeTimeoutSeconds: Property<Long>
    val rootBuildDirectory: DirectoryProperty
  }

  private sealed interface EndOfBuildAction {
    val runAfterFailure: Boolean

    data class Close(
      val searchForRepositoryIfNoIdPresent: Boolean,
    ) : EndOfBuildAction {
      override val runAfterFailure: Boolean = false
    }

    data object ReleaseAfterClose : EndOfBuildAction {
      override val runAfterFailure: Boolean = false
    }

    data class Drop(
      override val runAfterFailure: Boolean,
      val searchForRepositoryIfNoIdPresent: Boolean,
    ) : EndOfBuildAction
  }

  private val centralPortal by lazy {
    SonatypeCentralPortal(
      baseUrl = parameters.sonatypeHost.get().apiBaseUrl(),
      usertoken = Base64.getEncoder().encode(
        "${parameters.repositoryUsername.get()}:${parameters.repositoryPassword.get()}".toByteArray(),
      ).toString(Charsets.UTF_8),
      userAgentName = BuildConfig.NAME,
      userAgentVersion = BuildConfig.VERSION,
      okhttpTimeoutSeconds = parameters.okhttpTimeoutSeconds.get(),
      closeTimeoutSeconds = parameters.closeTimeoutSeconds.get(),
    )
  }

  private val nexus by lazy {
    Nexus(
      baseUrl = parameters.sonatypeHost.get().apiBaseUrl(),
      username = parameters.repositoryUsername.get(),
      password = parameters.repositoryPassword.get(),
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

  private var buildIsSuccess: Boolean = true

  /**
   * Is only be allowed to be called from task actions.
   */
  fun createStagingRepository() {
    if (parameters.versionIsSnapshot.get()) {
      return
    }

    if (uploadId != null) {
      return
    }

    uploadId = if (parameters.sonatypeHost.get().isCentralPortal) {
      UUID.randomUUID().toString()
    } else {
      nexus.createRepositoryForGroup(parameters.groupId.get())
    }

    endOfBuildActions += EndOfBuildAction.Close(searchForRepositoryIfNoIdPresent = false)
    if (parameters.automaticRelease.get()) {
      endOfBuildActions += EndOfBuildAction.ReleaseAfterClose
    }
    endOfBuildActions += EndOfBuildAction.Drop(
      runAfterFailure = true,
      searchForRepositoryIfNoIdPresent = false,
    )
  }

  /**
   * Is only be allowed to be called from task actions. Tasks calling this must run after tasks
   * that call [createStagingRepository].
   */
  fun shouldCloseAndReleaseRepository(manualStagingRepositoryId: String?) {
    if (manualStagingRepositoryId != null) {
      publishId = manualStagingRepositoryId
    } else {
      if (uploadId == null && parameters.sonatypeHost.get().isCentralPortal) {
        error("A deployment id needs to be provided with `--repository` when publishing through Central Portal")
      }
    }

    endOfBuildActions += EndOfBuildAction.Close(searchForRepositoryIfNoIdPresent = true)
    endOfBuildActions += EndOfBuildAction.ReleaseAfterClose
  }

  /**
   * Is only be allowed to be called from task actions. Tasks calling this must run after tasks
   * that call [createStagingRepository].
   */
  fun shouldDropRepository(manualStagingRepositoryId: String?) {
    if (manualStagingRepositoryId != null) {
      publishId = manualStagingRepositoryId
    } else {
      if (parameters.sonatypeHost.get().isCentralPortal) {
        error("A deployment id needs to be provided with `--repository` when publishing through Central Portal")
      }
    }

    endOfBuildActions += EndOfBuildAction.Drop(
      runAfterFailure = false,
      searchForRepositoryIfNoIdPresent = true,
    )
  }

  internal fun publishingUrl(): String {
    return if (parameters.versionIsSnapshot.get()) {
      require(uploadId == null) {
        "Staging repositories are not supported for SNAPSHOT versions."
      }

      val host = parameters.sonatypeHost.get()
      require(!host.isCentralPortal) {
        "Snapshots are not supported when publishing through the central portal."
      }

      "${host.rootUrl}/content/repositories/snapshots/"
    } else {
      val stagingRepositoryId = requireNotNull(uploadId) {
        @Suppress("UnstableApiUsage")
        if (buildFeatures.configurationCache.active.get()) {
          "Publishing releases to Maven Central is not supported yet with configuration caching enabled, because of " +
            "this missing Gradle feature: https://github.com/gradle/gradle/issues/22779"
        } else {
          "The staging repository was not created yet. Please open a bug with a build scan or build logs and stacktrace"
        }
      }

      val host = parameters.sonatypeHost.get()
      if (host.isCentralPortal) {
        "file://${parameters.rootBuildDirectory.get()}/publish/staging/$stagingRepositoryId"
      } else {
        "${host.rootUrl}/service/local/staging/deployByRepositoryId/$stagingRepositoryId/"
      }
    }
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
    if (parameters.sonatypeHost.get().isCentralPortal) {
      runCentralPortalEndOfBuildActions(actions)
    } else {
      runNexusEndOfBuildActions(actions)
    }
  }

  private fun runCentralPortalEndOfBuildActions(actions: List<EndOfBuildAction>) {
    val uploadId = uploadId

    val closeActions = actions.filterIsInstance<EndOfBuildAction.Close>()
    if (closeActions.isNotEmpty()) {
      if (uploadId != null) {
        val deploymentName = "${parameters.groupId.get()}-$uploadId"
        val publishingType = if (actions.contains(EndOfBuildAction.ReleaseAfterClose)) {
          "AUTOMATIC"
        } else {
          "USER_MANAGED"
        }

        val directory = File(publishingUrl().substringAfter("://"))
        val zipFile = File("${directory.absolutePath}.zip")
        val out = ZipOutputStream(FileOutputStream(zipFile))
        directory.walkTopDown().forEach {
          if (it.isDirectory) {
            return@forEach
          }
          if (it.name.contains("maven-metadata")) {
            return@forEach
          }

          val entry = ZipEntry(it.toRelativeString(directory))
          out.putNextEntry(entry)
          out.write(it.readBytes())
          out.closeEntry()
        }
        out.close()

        publishId = centralPortal.upload(deploymentName, publishingType, zipFile)
      } else {
        val publishId = publishId
        if (publishId != null) {
          centralPortal.publishDeployment(publishId)
        } else if (closeActions.all { it.searchForRepositoryIfNoIdPresent }) {
          error("A deployment id needs to be provided when publishing through Central Portal")
        }
      }
    }

    val dropAction = actions.filterIsInstance<EndOfBuildAction.Drop>().singleOrNull()
    if (dropAction != null) {
      val publishId = publishId
      if (publishId != null) {
        centralPortal.deleteDeployment(publishId)
      } else if (dropAction.searchForRepositoryIfNoIdPresent) {
        error("A deployment id needs to be provided when publishing through Central Portal")
      }
    }
  }

  private fun runNexusEndOfBuildActions(actions: List<EndOfBuildAction>) {
    var stagingRepositoryId = uploadId ?: publishId

    val closeActions = actions.filterIsInstance<EndOfBuildAction.Close>()
    if (closeActions.isNotEmpty()) {
      if (stagingRepositoryId != null) {
        nexus.closeStagingRepository(stagingRepositoryId)
      } else if (closeActions.all { it.searchForRepositoryIfNoIdPresent }) {
        stagingRepositoryId = nexus.closeCurrentStagingRepository()
      }

      if (stagingRepositoryId != null && actions.contains(EndOfBuildAction.ReleaseAfterClose)) {
        nexus.releaseStagingRepository(stagingRepositoryId)
      }
    }

    // there might be 2 drop actions but one of the runs on success, the other on failure
    val dropAction = actions.filterIsInstance<EndOfBuildAction.Drop>().singleOrNull()
    if (dropAction != null) {
      if (stagingRepositoryId != null) {
        nexus.dropStagingRepository(stagingRepositoryId)
      } else if (dropAction.searchForRepositoryIfNoIdPresent) {
        nexus.dropCurrentStagingRepository()
      }
    }
  }

  companion object {
    private const val NAME = "sonatype-repository-build-service"

    fun Project.registerSonatypeRepositoryBuildService(
      sonatypeHost: Provider<SonatypeHost>,
      groupId: Provider<String>,
      versionIsSnapshot: Provider<Boolean>,
      repositoryUsername: Provider<String>,
      repositoryPassword: Provider<String>,
      automaticRelease: Boolean,
      rootBuildDirectory: Provider<Directory>,
      buildEventsListenerRegistry: BuildEventsListenerRegistry,
    ): Provider<SonatypeRepositoryBuildService> {
      val okhttpTimeout = project.providers.gradleProperty("SONATYPE_CONNECT_TIMEOUT_SECONDS")
        .map { it.toLong() }
        .orElse(60)
      val closeTimeout = project.providers.gradleProperty("SONATYPE_CLOSE_TIMEOUT_SECONDS")
        .map { it.toLong() }
        .orElse(60 * 15)
      val service = gradle.sharedServices.registerIfAbsent(NAME, SonatypeRepositoryBuildService::class.java) {
        it.maxParallelUsages.set(1)
        it.parameters.sonatypeHost.set(sonatypeHost)
        it.parameters.groupId.set(groupId)
        it.parameters.versionIsSnapshot.set(versionIsSnapshot)
        it.parameters.repositoryUsername.set(repositoryUsername)
        it.parameters.repositoryPassword.set(repositoryPassword)
        it.parameters.automaticRelease.set(automaticRelease)
        it.parameters.okhttpTimeoutSeconds.set(okhttpTimeout)
        it.parameters.closeTimeoutSeconds.set(closeTimeout)
        it.parameters.rootBuildDirectory.set(rootBuildDirectory)
      }
      buildEventsListenerRegistry.onTaskCompletion(service)
      return service
    }
  }
}
