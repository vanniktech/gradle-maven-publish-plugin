package com.vanniktech.maven.publish.sonatype

import com.vanniktech.maven.publish.BuildConfig
import com.vanniktech.maven.publish.SonatypeHost
import com.vanniktech.maven.publish.nexus.Nexus
import java.io.IOException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.build.event.BuildEventsListenerRegistry
import org.gradle.configurationcache.extensions.serviceOf
import org.gradle.tooling.events.FailureResult
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener

internal abstract class SonatypeRepositoryBuildService :
  BuildService<SonatypeRepositoryBuildService.Params>, AutoCloseable, OperationCompletionListener {
  private val logger: Logger = Logging.getLogger(SonatypeRepositoryBuildService::class.java)

  internal interface Params : BuildServiceParameters {
    val sonatypeHost: Property<SonatypeHost>
    val groupId: Property<String>
    val versionIsSnapshot: Property<Boolean>
    val repositoryUsername: Property<String>
    val repositoryPassword: Property<String>
    val automaticRelease: Property<Boolean>
    val okhttpTimeoutSeconds: Property<Long>
    val closeTimeoutSeconds: Property<Long>
  }

  private sealed interface EndOfBuildAction {
    val runAfterFailure: Boolean

    data class Close(
      val searchForRepositoryIfNoIdPresent: Boolean
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

  private var stagingRepositoryId: String? = null
    set(value) {
      check (field != null && field != value) {
        "stagingRepositoryId was already set to '$field', new value '$value'"
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

    if (stagingRepositoryId != null) {
      return
    }

    stagingRepositoryId = nexus.createRepositoryForGroup(parameters.groupId.get())
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
      stagingRepositoryId = manualStagingRepositoryId
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
      stagingRepositoryId = manualStagingRepositoryId
    }

    endOfBuildActions += EndOfBuildAction.Drop(
      runAfterFailure = false,
      searchForRepositoryIfNoIdPresent = true,
    )
  }

  internal fun publishingUrl(configCacheEnabled: Boolean): String {
    return if (parameters.versionIsSnapshot.get()) {
      require(stagingRepositoryId == null) {
        "Staging repositories are not supported for SNAPSHOT versions."
      }
      "${parameters.sonatypeHost.get().rootUrl}/content/repositories/snapshots/"
    } else {
      val stagingRepositoryId = requireNotNull(stagingRepositoryId) {
        if (configCacheEnabled) {
          "Publishing releases to Maven Central is not supported yet with configuration caching enabled, because of " +
            "this missing Gradle feature: https://github.com/gradle/gradle/issues/22779"
        } else {
          "The staging repository was not created yet. Please open a bug with a build scan or build logs and stacktrace"
        }
      }

      "${parameters.sonatypeHost.get().rootUrl}/service/local/staging/deployByRepositoryId/$stagingRepositoryId/"
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
          logger.info("Failed processing $stagingRepositoryId staging repository after previous build failure", e)
        }
      }
    }
  }

  private fun runEndOfBuildActions(actions: List<EndOfBuildAction>) {
    var stagingRepositoryId = stagingRepositoryId

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
      }
      project.serviceOf<BuildEventsListenerRegistry>().onTaskCompletion(service)
      return service
    }
  }
}
