package com.vanniktech.maven.publish.sonatype

import javax.inject.Inject
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
import org.gradle.internal.logging.progress.ProgressLoggerFactory

internal abstract class CloseAndReleaseSonatypeRepositoryTask @Inject constructor(
  private val progressLoggerFactory: ProgressLoggerFactory,
) : DefaultTask() {

  @get:Internal
  abstract val buildService: Property<SonatypeRepositoryBuildService>

  @get:Internal
  abstract val usingPlainConsole: Property<Boolean>

  @Option(option = "repository", description = "Specify which staging repository to close and release.")
  @Input
  @Optional
  var manualStagingRepositoryId: String? = null

  @TaskAction
  fun closeAndReleaseRepository() {
    val service = this.buildService.get()

    // if repository was already closed in this build this is a no-op
    if (service.repositoryClosed) {
      return
    }

    val progressLogger = progressLoggerFactory.newOperation(CloseAndReleaseSonatypeRepositoryTask::class.java)
    val nexusProgressLogger = NexusProgressLogger(usingPlainConsole.get(), progressLogger)

    val manualStagingRepositoryId = this.manualStagingRepositoryId
    if (manualStagingRepositoryId != null) {
      nexusProgressLogger.start("Closing and releasing repository $manualStagingRepositoryId", "Close and release repository")
      service.nexus.closeStagingRepository(manualStagingRepositoryId, nexusProgressLogger)
      service.nexus.releaseStagingRepository(manualStagingRepositoryId, nexusProgressLogger)
    } else {
      nexusProgressLogger.start("Closing and releasing current repository", "Close and release repository")
      val id = service.nexus.closeCurrentStagingRepository(nexusProgressLogger)
      service.nexus.releaseStagingRepository(id, nexusProgressLogger)
    }

    service.repositoryClosed = true
  }

  companion object {
    private const val NAME = "closeAndReleaseRepository"

    fun TaskContainer.registerCloseAndReleaseRepository(
      isUsingPlainConsole: Boolean,
      buildService: Provider<SonatypeRepositoryBuildService>,
    ): TaskProvider<CloseAndReleaseSonatypeRepositoryTask> {
      return register(NAME, CloseAndReleaseSonatypeRepositoryTask::class.java) {
        it.description = "Closes and releases a staging repository on Sonatype OSS"
        it.group = "release"
        it.usingPlainConsole.set(isUsingPlainConsole)
        it.buildService.set(buildService)
        it.usesService(buildService)
      }
    }
  }
}

