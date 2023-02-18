package com.vanniktech.maven.publish.sonatype

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.logging.progress.ProgressLoggerFactory

internal abstract class CreateSonatypeRepositoryTask @Inject constructor(
  private val progressLoggerFactory: ProgressLoggerFactory,
) : DefaultTask() {

  @get:Internal
  abstract val projectGroup: Property<String>

  @get:Internal
  abstract val versionIsSnapshot: Property<Boolean>

  @get:Internal
  abstract val stagingRepositoryId: Property<String>

  @get:Internal
  abstract val buildService: Property<SonatypeRepositoryBuildService>

  @TaskAction
  fun createStagingRepository() {
    if (versionIsSnapshot.get()) {
      return
    }

    val service = this.buildService.get()

    // if repository was already created in this build this is a no-op
    val currentStagingRepositoryId = service.stagingRepositoryId
    if (currentStagingRepositoryId != null) {
      stagingRepositoryId.set(currentStagingRepositoryId)
      return
    }

    val progressLogger = progressLoggerFactory.newOperation(CreateSonatypeRepositoryTask::class.java)
    val nexusLogger = NexusProgressLogger(progressLogger)
    val id = service.nexus.createRepositoryForGroup(projectGroup.get(), nexusLogger)

    service.stagingRepositoryId = id
    stagingRepositoryId.set(id)
  }

  companion object {
    private const val NAME = "createStagingRepository"

    fun TaskContainer.registerCreateRepository(
      projectGroup: Provider<String>,
      versionIsSnapshot: Provider<Boolean>,
      buildService: Provider<SonatypeRepositoryBuildService>,
    ): TaskProvider<CreateSonatypeRepositoryTask> {
      return register(NAME, CreateSonatypeRepositoryTask::class.java) {
        it.description = "Create a staging repository on Sonatype OSS"
        it.group = "release"
        it.projectGroup.set(projectGroup)
        it.versionIsSnapshot.set(versionIsSnapshot)
        it.buildService.set(buildService)
        it.usesService(buildService)
      }
    }
  }
}
