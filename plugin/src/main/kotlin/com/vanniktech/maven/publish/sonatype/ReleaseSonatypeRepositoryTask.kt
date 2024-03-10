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

internal abstract class ReleaseSonatypeRepositoryTask : DefaultTask() {
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

    fun TaskContainer.registerReleaseRepository(
      buildService: Provider<SonatypeRepositoryBuildService>,
      createRepository: TaskProvider<CreateSonatypeRepositoryTask>,
    ): TaskProvider<ReleaseSonatypeRepositoryTask> {
      return register(NAME, ReleaseSonatypeRepositoryTask::class.java) {
        it.description = "Releases a staging repository on Sonatype OSS"
        it.group = "release"
        it.buildService.set(buildService)
        it.usesService(buildService)
        it.mustRunAfter(createRepository)
      }
    }
  }
}
