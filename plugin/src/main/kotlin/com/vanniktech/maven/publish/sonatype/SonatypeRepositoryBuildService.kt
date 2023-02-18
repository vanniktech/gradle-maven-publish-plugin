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

internal abstract class SonatypeRepositoryBuildService : BuildService<SonatypeRepositoryBuildService.Params>, AutoCloseable, OperationCompletionListener {

  private val logger: Logger = Logging.getLogger(SonatypeRepositoryBuildService::class.java)

  internal interface Params : BuildServiceParameters {
    val sonatypeHost: Property<SonatypeHost>
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

  // should only be accessed from CreateSonatypeRepositoryTask
  // for all other use cases use MavenPublishBaseExtension
  // the id of the staging repository that was created during this build
  var stagingRepositoryId: String? = null
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

  override fun onFinish(event: FinishEvent) {
    if (event.result is FailureResult) {
      buildIsSuccess = false
    }
  }

  override fun close() {
    val stagingRepositoryId = this.stagingRepositoryId
    if (stagingRepositoryId != null) {
      if (buildIsSuccess) {
        nexus.closeStagingRepository(stagingRepositoryId, Nexus.Logger.SystemLogger)
        if (parameters.automaticRelease.get()) {
          nexus.releaseStagingRepository(stagingRepositoryId, Nexus.Logger.SystemLogger)
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

    @Suppress("UnstableApiUsage")
    fun Project.registerSonatypeRepositoryBuildService(
      sonatypeHost: Provider<SonatypeHost>,
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
