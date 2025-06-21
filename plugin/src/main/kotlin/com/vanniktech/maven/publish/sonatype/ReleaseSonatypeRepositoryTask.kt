package com.vanniktech.maven.publish.sonatype

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal abstract class ReleaseSonatypeRepositoryTask : DefaultTask() {
  @get:Internal
  abstract val buildService: Property<SonatypeRepositoryBuildService>

  @TaskAction
  fun enableAutomaticPublishing() {
    buildService.get().enableAutomaticPublishing()
  }

  companion object {
    private const val NAME = "releaseRepository"

    fun TaskContainer.registerReleaseRepository(
      buildService: Provider<SonatypeRepositoryBuildService>,
    ): TaskProvider<ReleaseSonatypeRepositoryTask> = register(NAME, ReleaseSonatypeRepositoryTask::class.java) {
      it.description = "Releases a staging repository on Sonatype OSS"
      it.group = "release"
      it.buildService.set(buildService)
      it.usesService(buildService)
    }
  }
}
