package com.vanniktech.maven.publish.sonatype

import com.vanniktech.maven.publish.SonatypeHost
import com.vanniktech.maven.publish.nexus.Nexus
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.services.BuildServiceRegistry
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.gradle.tooling.events.task.TaskFailureResult

internal abstract class SonatypeRepositoryBuildService : BuildService<SonatypeRepositoryBuildService.Params>, AutoCloseable, OperationCompletionListener {
  internal interface Params : BuildServiceParameters {
    val sonatypeHost: Property<SonatypeHost>
    val repositoryUsername: Property<String>
    val repositoryPassword: Property<String>
    val automaticRelease: Property<Boolean>
  }

  val nexus = Nexus(
    baseUrl = parameters.sonatypeHost.get().apiBaseUrl(),
    username = parameters.repositoryUsername.get(),
    password = parameters.repositoryPassword.get(),
  )

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

  var buildHasFailure: Boolean = false

  override fun onFinish(event: FinishEvent) {
    if (event.result is TaskFailureResult) {
      buildHasFailure = true
    }
  }

  override fun close() {
    if (buildHasFailure) {
      return
    }

    val stagingRepositoryId = this.stagingRepositoryId
    if (stagingRepositoryId != null) {
      nexus.closeStagingRepository(stagingRepositoryId)
      if (parameters.automaticRelease.get()) {
        nexus.releaseStagingRepository(stagingRepositoryId)
      }
    }
  }

  companion object {
    private const val NAME = "sonatype-repository-build-service"

    fun BuildServiceRegistry.registerSonatypeRepositoryBuildService(
      sonatypeHost: Provider<SonatypeHost>,
      repositoryUsername: Provider<String>,
      repositoryPassword: Provider<String>,
      automaticRelease: Boolean,
    ): Provider<SonatypeRepositoryBuildService> {
      return registerIfAbsent(NAME, SonatypeRepositoryBuildService::class.java) {
        it.maxParallelUsages.set(1)
        it.parameters.sonatypeHost.set(sonatypeHost)
        it.parameters.repositoryUsername.set(repositoryUsername)
        it.parameters.repositoryPassword.set(repositoryPassword)
        it.parameters.automaticRelease.set(automaticRelease)
      }
    }
  }
}
