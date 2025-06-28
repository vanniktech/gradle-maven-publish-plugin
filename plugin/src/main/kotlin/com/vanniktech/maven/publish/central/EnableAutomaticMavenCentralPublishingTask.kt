package com.vanniktech.maven.publish.central

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal abstract class EnableAutomaticMavenCentralPublishingTask : DefaultTask() {
  @get:Internal
  abstract val buildService: Property<MavenCentralBuildService>

  @TaskAction
  fun enableAutomaticPublishing() {
    buildService.get().enableAutomaticPublishing()
  }

  companion object {
    private const val NAME = "enableAutomaticMavenCentralPublishing"

    fun TaskContainer.registerEnableAutomaticMavenCentralPublishingTask(
      buildService: Provider<MavenCentralBuildService>,
    ): TaskProvider<EnableAutomaticMavenCentralPublishingTask> = register(NAME, EnableAutomaticMavenCentralPublishingTask::class.java) {
      it.description = "Enables automatic publishing for Maven Central"
      it.buildService.set(buildService)
      it.usesService(buildService)
    }
  }
}
