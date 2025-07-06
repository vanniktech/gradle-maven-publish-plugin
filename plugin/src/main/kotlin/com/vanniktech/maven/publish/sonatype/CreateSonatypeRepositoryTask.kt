package com.vanniktech.maven.publish.sonatype

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal abstract class CreateSonatypeRepositoryTask : DefaultTask() {
  @get:Internal
  abstract val projectGroup: Property<String>

  @get:Input
  abstract val artifactId: Property<String>

  @get:Input
  abstract val version: Property<String>

  @get:Internal
  abstract val buildService: Property<SonatypeRepositoryBuildService>

  @TaskAction
  fun createStagingRepository() {
    buildService.get().createStagingRepository(projectGroup.get(), artifactId.get(), version.get())
  }

  companion object {
    private const val NAME = "createStagingRepository"

    fun TaskContainer.registerCreateRepository(
      buildService: Provider<SonatypeRepositoryBuildService>,
      group: Provider<String>,
      artifactId: Provider<String>,
      version: Provider<String>,
    ): TaskProvider<CreateSonatypeRepositoryTask> = register(NAME, CreateSonatypeRepositoryTask::class.java) {
      it.description = "Create a staging repository on Sonatype OSS"
      it.group = "release"
      it.projectGroup.set(group)
      it.artifactId.set(artifactId)
      it.version.set(version)
      it.buildService.set(buildService)
      it.usesService(buildService)
    }
  }
}
