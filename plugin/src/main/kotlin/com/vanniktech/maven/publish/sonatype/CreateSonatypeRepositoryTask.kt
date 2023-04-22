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
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor

internal abstract class CreateSonatypeRepositoryTask : DefaultTask() {

  @get:Internal
  abstract val projectGroup: Property<String>

  @get:Internal
  abstract val versionIsSnapshot: Property<Boolean>

  @get:Internal
  abstract val usingPlainConsole: Property<Boolean>

  @get:Internal
  abstract val buildService: Property<SonatypeRepositoryBuildService>

  @Inject
  abstract fun getWorkerExecutor(): WorkerExecutor

  @TaskAction
  fun createStagingRepository() {
    val workQueue: WorkQueue = getWorkerExecutor().noIsolation()
    workQueue.submit(CreateStagingRepository::class.java) {
      requireNotNull(it)
      it.projectGroup.set(projectGroup)
      it.versionIsSnapshot.set(versionIsSnapshot)
      it.usingPlainConsole.set(usingPlainConsole)
      it.buildService.set(buildService)
    }
  }

  internal interface CreateStagingRepositoryParameters : WorkParameters {
    val projectGroup: Property<String>
    val versionIsSnapshot: Property<Boolean>
    val usingPlainConsole: Property<Boolean>
    val buildService: Property<SonatypeRepositoryBuildService>
  }

  abstract class CreateStagingRepository @Inject constructor(
    private val progressLoggerFactory: ProgressLoggerFactory
  ) : WorkAction<CreateStagingRepositoryParameters> {
    override fun execute() {
      val parameters = requireNotNull(parameters)
      if (parameters.versionIsSnapshot.get()) {
        return
      }

      val service = parameters.buildService.get()

      // if repository was already created in this build this is a no-op
      val currentStagingRepositoryId = service.stagingRepositoryId
      if (currentStagingRepositoryId != null) {
        return
      }

      val progressLogger = progressLoggerFactory.newOperation(CreateSonatypeRepositoryTask::class.java)
      val nexusLogger = NexusProgressLogger(parameters.usingPlainConsole.get(), progressLogger)

      val id = service.nexus.createRepositoryForGroup(parameters.projectGroup.get(), nexusLogger)
      service.stagingRepositoryId = id
    }
  }

  companion object {
    private const val NAME = "createStagingRepository"

    fun TaskContainer.registerCreateRepository(
      projectGroup: Provider<String>,
      versionIsSnapshot: Provider<Boolean>,
      usingPlainConsole: Boolean,
      buildService: Provider<SonatypeRepositoryBuildService>,
    ): TaskProvider<CreateSonatypeRepositoryTask> {
      return register(NAME, CreateSonatypeRepositoryTask::class.java) {
        it.description = "Create a staging repository on Sonatype OSS"
        it.group = "release"
        it.projectGroup.set(projectGroup)
        it.versionIsSnapshot.set(versionIsSnapshot)
        it.usingPlainConsole.set(usingPlainConsole)
        it.buildService.set(buildService)
        it.usesService(buildService)
      }
    }
  }
}
