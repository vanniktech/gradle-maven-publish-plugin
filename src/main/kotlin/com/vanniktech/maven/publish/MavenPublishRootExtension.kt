package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.nexus.CloseAndReleaseRepositoryTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

@Suppress("UnnecessaryAbstractClass")
internal abstract class MavenPublishRootExtension(
  private val project: Project
) {

  private var closeAndReleaseTask: TaskProvider<CloseAndReleaseRepositoryTask>? = null

  internal fun configureCloseAndReleaseTask(baseUrl: String, repositoryUsername: String?, repositoryPassword: String?) {
    if (closeAndReleaseTask != null) {
      return
    }

    closeAndReleaseTask = project.tasks.register("closeAndReleaseRepository", CloseAndReleaseRepositoryTask::class.java) {
      it.description = "Closes and releases an artifacts repository in Nexus"
      it.group = "release"
      it.baseUrl = baseUrl
      it.repositoryUsername = repositoryUsername
      it.repositoryPassword = repositoryPassword
    }
  }
}
