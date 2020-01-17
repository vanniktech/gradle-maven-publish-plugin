package com.vanniktech.maven.publish

import org.gradle.api.component.SoftwareComponent
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.AbstractArchiveTask

internal interface Configurer {
  /**
   * Needs to be called for all targets before `addComponent` and
   * `addTaskOutput`.
   */
  fun configureTarget(target: MavenPublishTarget)

  fun addComponent(component: SoftwareComponent)

  fun addTaskOutput(taskProvider: TaskProvider<AbstractArchiveTask>)
}
