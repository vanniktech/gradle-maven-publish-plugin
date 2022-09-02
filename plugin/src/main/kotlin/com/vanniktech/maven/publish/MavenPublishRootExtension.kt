package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.sonatype.CloseAndReleaseSonatypeRepositoryTask
import com.vanniktech.maven.publish.sonatype.CloseAndReleaseSonatypeRepositoryTask.Companion.registerCloseAndReleaseRepository
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider

internal abstract class MavenPublishRootExtension(
  private val project: Project
) {

  private var closeAndReleaseTask: TaskProvider<CloseAndReleaseSonatypeRepositoryTask>? = null

  internal fun configureCloseAndReleaseTask(
    baseUrl: Provider<String>,
    repositoryUsername: Provider<String>,
    repositoryPassword: Provider<String>,
  ) {
    if (closeAndReleaseTask != null) {
      return
    }

    closeAndReleaseTask = project.tasks
      .registerCloseAndReleaseRepository(baseUrl, repositoryUsername, repositoryPassword)
  }
}
