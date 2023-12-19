package com.vanniktech.maven.publish.sonatype

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.options.Option

internal abstract class DropSonatypeRepositoryTask : DefaultTask() {
  @get:Internal
  abstract val buildService: Property<SonatypeRepositoryBuildService>

  @Option(option = "repository", description = "Specify which staging repository to drop.")
  @Input
  @Optional
  var manualStagingRepositoryId: String? = null

  @TaskAction
  fun closeAndReleaseRepository() {
    val service = this.buildService.get()

    // if repository was already dropped in this build this is a no-op
    if (service.repositoryDropped) {
      return
    }

    val manualStagingRepositoryId = this.manualStagingRepositoryId
    if (manualStagingRepositoryId != null) {
      service.nexus.dropStagingRepository(manualStagingRepositoryId)
    } else {
      service.nexus.dropCurrentStagingRepository()
    }

    service.repositoryDropped = true
  }

  companion object {
    private const val NAME = "dropRepository"

    fun TaskContainer.registerDropRepository(
      buildService: Provider<SonatypeRepositoryBuildService>,
    ): TaskProvider<DropSonatypeRepositoryTask> {
      return register(NAME, DropSonatypeRepositoryTask::class.java) {
        it.description = "Drops a staging repository on Sonatype OSS"
        it.group = "release"
        it.buildService.set(buildService)
        it.usesService(buildService)
      }
    }
  }
}
