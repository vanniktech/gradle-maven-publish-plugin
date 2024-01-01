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

  val nexus by lazy {
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
      if (field != null) {
        throw IllegalStateException("stagingRepositoryId was already set")
      }
      field = value
    }

  // should only be accessed from CloseAndReleaseSonatypeRepositoryTask
  // indicates whether we already closed a staging repository to avoid doing it more than once in a build
  var repositoryClosed: Boolean = false

  // should only be accessed from DropSonatypeRepositoryTask
  // indicates whether we already closed a staging repository to avoid doing it more than once in a build
  var repositoryDropped: Boolean = false

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

    this.stagingRepositoryId = nexus.createRepositoryForGroup(parameters.groupId.get())
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
    val stagingRepositoryId = this.stagingRepositoryId
    if (stagingRepositoryId != null) {
      if (buildIsSuccess) {
        nexus.closeStagingRepository(stagingRepositoryId)
        if (parameters.automaticRelease.get()) {
          nexus.releaseStagingRepository(stagingRepositoryId)
        }
      } else {
        try {
          nexus.dropStagingRepository(stagingRepositoryId)
        } catch (e: IOException) {
          logger.info("Failed to drop staging repository $stagingRepositoryId", e)
        }
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
