package com.vanniktech.maven.publish.nexus

import org.gradle.api.Project

class NexusConfigurer(project: Project) {
  init {
    val task = project.tasks.create<CloseAndReleaseRepositoryTask>("closeAndReleaseRepository", CloseAndReleaseRepositoryTask::class.java)
    task.description = "Closes and releases an artifacts repository in Nexus"
    task.group = "release"
  }
}
