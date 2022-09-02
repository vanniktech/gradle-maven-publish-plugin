package com.vanniktech.maven.publish.sonatype

import com.vanniktech.maven.publish.nexus.Nexus
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.options.Option

internal abstract class CloseAndReleaseSonatypeRepositoryTask : DefaultTask() {

  @get:Input
  abstract val baseUrl: Property<String>

  @get:Input
  abstract val repositoryUsername: Property<String>

  @get:Input
  abstract val repositoryPassword: Property<String>

  @Option(option = "repository", description = "Specify which staging repository to close and release.")
  @Input
  @Optional
  var manualStagingRepositoryId: String? = null

  @TaskAction
  fun closeAndReleaseRepository() {
    val nexus = Nexus(
      baseUrl = baseUrl.get(),
      username = repositoryUsername.get(),
      password = repositoryPassword.get(),
    )

    val manualStagingRepositoryId = this.manualStagingRepositoryId
    if (manualStagingRepositoryId != null) {
      nexus.closeAndReleaseRepositoryById(manualStagingRepositoryId)
    } else {
      nexus.closeAndReleaseCurrentRepository()
    }
  }

  companion object {
    private const val NAME = "closeAndReleaseRepository"

    fun TaskContainer.registerCloseAndReleaseRepository(
      baseUrl: Provider<String>,
      repositoryUsername: Provider<String>,
      repositoryPassword: Provider<String>,
    ): TaskProvider<CloseAndReleaseSonatypeRepositoryTask> {
      return register(NAME, CloseAndReleaseSonatypeRepositoryTask::class.java) {
        it.description = "Closes and releases a staging repository on Sonatype OSS"
        it.group = "release"
        it.baseUrl.set(baseUrl)
        it.repositoryUsername.set(repositoryUsername)
        it.repositoryPassword.set(repositoryPassword)
      }
    }
  }
}
