package com.vanniktech.maven.publish.legacy

import org.gradle.api.Project

internal fun Project.configureArchivesTasks() {
  tasks.register("uploadArchives") { task ->
    val publishTaskName = "publishAllPublicationsToMavenCentralRepository"
    task.dependsOn(tasks.named(publishTaskName))

    task.doLast {
      logger.warn("The task ${task.name} is deprecated use publish or $publishTaskName instead.")
    }
  }
  tasks.register("installArchives") { task ->
    val publishTaskName = "publishToMavenLocal"
    task.dependsOn(tasks.named(publishTaskName))

    task.doLast {
      logger.warn("The task ${task.name} is deprecated use $publishTaskName instead.")
    }
  }
}
