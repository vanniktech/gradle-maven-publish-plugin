package com.vanniktech.maven.publish.sonatype

import com.vanniktech.maven.publish.SonatypeHost
import com.vanniktech.maven.publish.nexus.Nexus
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.services.BuildServiceRegistry

internal abstract class SonatypeRepositoryBuildService : BuildService<SonatypeRepositoryBuildService.Params> {
  internal interface Params : BuildServiceParameters {
    val sonatypeHost: Property<SonatypeHost>
    val repositoryUsername: Property<String>
    val repositoryPassword: Property<String>
  }

  val nexus = Nexus(
    baseUrl = parameters.sonatypeHost.get().apiBaseUrl(),
    username = parameters.repositoryUsername.get(),
    password = parameters.repositoryPassword.get(),
  )

  // should only be accessed from CloseAndReleaseSonatypeRepositoryTask
  // indicates whether we already closed a staging repository to avoid doing it more than once in a build
  var repositoryClosed: Boolean = false

  companion object {
    private const val NAME = "sonatype-repository-build-service"

    fun BuildServiceRegistry.registerSonatypeRepositoryBuildService(
      sonatypeHost: Provider<SonatypeHost>,
      repositoryUsername: Provider<String>,
      repositoryPassword: Provider<String>,
    ): Provider<SonatypeRepositoryBuildService> {
      return registerIfAbsent(NAME, SonatypeRepositoryBuildService::class.java) {
        it.maxParallelUsages.set(1)
        it.parameters.sonatypeHost.set(sonatypeHost)
        it.parameters.repositoryUsername.set(repositoryUsername)
        it.parameters.repositoryPassword.set(repositoryPassword)
      }
    }
  }
}
