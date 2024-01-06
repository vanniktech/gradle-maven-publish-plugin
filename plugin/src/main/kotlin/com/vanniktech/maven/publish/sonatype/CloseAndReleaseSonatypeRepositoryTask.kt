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

internal abstract class CloseAndReleaseSonatypeRepositoryTask : DefaultTask() {
  @get:Internal
  abstract val buildService: Property<SonatypeRepositoryBuildService>

  @Option(option = "repository", description = "Specify which staging repository to close and release.")
  @Input
  @Optional
  var manualStagingRepositoryId: String? = null

  @TaskAction
  fun closeAndReleaseRepository() {
    val service = this.buildService.get()
    service.shouldCloseAndReleaseRepository(manualStagingRepositoryId)
  }

  companion object {
    private const val NAME = "releaseRepository"
    private const val LEGACY_NAME = "closeAndReleaseRepository"

    fun TaskContainer.registerReleaseRepository(
      buildService: Provider<SonatypeRepositoryBuildService>,
      createRepository: TaskProvider<CreateSonatypeRepositoryTask>,
    ): TaskProvider<CloseAndReleaseSonatypeRepositoryTask> {
      return register(NAME, CloseAndReleaseSonatypeRepositoryTask::class.java) {
        it.description = "Releases a staging repository on Sonatype OSS"
        it.group = "release"
        it.buildService.set(buildService)
        it.usesService(buildService)
        it.mustRunAfter(createRepository)
      }
    }

    fun TaskContainer.registerCloseAndReleaseRepository(
      buildService: Provider<SonatypeRepositoryBuildService>,
      createRepository: TaskProvider<CreateSonatypeRepositoryTask>,
    ): TaskProvider<CloseAndReleaseSonatypeRepositoryTask> {
      return register(LEGACY_NAME, CloseAndReleaseSonatypeRepositoryTask::class.java) {
        it.description = "Closes and releases a staging repository on Sonatype OSS"
        it.group = "release"
        it.buildService.set(buildService)
        it.usesService(buildService)
        it.mustRunAfter(createRepository)
        it.doLast { task ->
          task.logger.warn("$LEGACY_NAME is deprecated and will be removed in a future release, use $NAME instead.")
        }
      }
    }
  }
}
