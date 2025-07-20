package com.vanniktech.maven.publish.internal.central

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.UntrackedTask
import org.gradle.api.tasks.options.Option

@UntrackedTask(because = "Not worth tracking")
internal abstract class DropMavenCentralDeploymentTask : DefaultTask() {
  @get:Internal
  abstract val buildService: Property<MavenCentralBuildService>

  @get:Input
  @get:Option(option = "deployment-id", description = "Specify which deployment to drop.")
  abstract val deploymentId: Property<String>

  @TaskAction
  fun dropDeployment() {
    buildService.get().dropDeployment(deploymentId.get())
  }

  companion object {
    private const val NAME = "dropMavenCentralDeployment"

    fun TaskContainer.registerDropMavenCentralDeploymentTask(
      buildService: Provider<MavenCentralBuildService>,
    ): TaskProvider<DropMavenCentralDeploymentTask> = register(NAME, DropMavenCentralDeploymentTask::class.java) {
      it.description = "Drops the deployment with the supplied id"
      it.group = "publishing"
      it.buildService.set(buildService)
      it.usesService(buildService)
    }
  }
}
