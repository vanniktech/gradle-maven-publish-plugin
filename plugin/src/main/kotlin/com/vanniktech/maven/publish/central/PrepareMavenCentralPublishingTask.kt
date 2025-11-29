package com.vanniktech.maven.publish.central

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.UntrackedTask

@UntrackedTask(because = "Not worth tracking")
internal abstract class PrepareMavenCentralPublishingTask : DefaultTask() {
  @get:Internal
  abstract val projectGroup: Property<String>

  @get:Input
  abstract val artifactId: Property<String>

  @get:Input
  abstract val version: Property<String>

  @get:Internal
  abstract val localRepository: DirectoryProperty

  @get:Internal
  abstract val buildService: Property<MavenCentralBuildService>

  @TaskAction
  fun registerProject() {
    val localRepository = localRepository.asFile.get()

    // delete local repository from previous publishing attempts to ensure only current files are published.
    if (localRepository.exists()) {
      localRepository.deleteRecursively()
    }

    check(
      buildService
        .get()
        .parameters.repositoryUsername.isPresent,
    ) {
      "mavenCentralUsername not found, which is required for publishing to Maven Central."
    }
    check(
      buildService
        .get()
        .parameters.repositoryPassword.isPresent,
    ) {
      "mavenCentralPassword not found, which is required for publishing to Maven Central."
    }

    buildService.get().registerProject(projectGroup.get(), artifactId.get(), version.get(), localRepository)
  }

  companion object {
    private const val NAME = "prepareMavenCentralPublishing"

    fun TaskContainer.registerPrepareMavenCentralPublishingTask(
      buildService: Provider<MavenCentralBuildService>,
      group: Provider<String>,
      artifactId: Provider<String>,
      version: Provider<String>,
      localRepository: Provider<Directory>,
    ): TaskProvider<PrepareMavenCentralPublishingTask> = register(NAME, PrepareMavenCentralPublishingTask::class.java) {
      it.description = "Prepare for publishing to Maven Central"
      it.projectGroup.set(group)
      it.artifactId.set(artifactId)
      it.version.set(version)
      it.localRepository.set(localRepository)
      it.buildService.set(buildService)
      it.usesService(buildService)
    }
  }
}
