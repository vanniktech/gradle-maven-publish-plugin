package com.vanniktech.maven.publish

import org.gradle.api.component.SoftwareComponent
import org.gradle.api.tasks.bundling.AbstractArchiveTask

internal interface Configurer {
  fun configureTarget(target: MavenPublishTarget)
  fun addComponent(component: SoftwareComponent)
  fun addTaskOutput(task: AbstractArchiveTask)
}
