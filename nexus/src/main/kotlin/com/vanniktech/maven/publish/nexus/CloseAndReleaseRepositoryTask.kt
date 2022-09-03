package com.vanniktech.maven.publish.nexus

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

open class CloseAndReleaseRepositoryTask : DefaultTask() {

  @Input
  var baseUrl: String? = null

  @Input
  var repositoryUsername: String? = null

  @Input
  var repositoryPassword: String? = null

  @Option(option = "repository", description = "Specify which staging repository to close and release.")
  @Input
  @Optional
  var manualStagingRepositoryId: String? = null

  @TaskAction
  fun closeAndReleaseRepository() {
    val baseUrl = requireNotNull(baseUrl) {
      "Please set a value for nexus.baseUrl"
    }
    val repositoryUsername = requireNotNull(repositoryUsername) {
      "Please set a value for nexus.repositoryUsername"
    }
    val repositoryPassword = requireNotNull(repositoryPassword) {
      "Please set a value for nexus.repositoryPassword"
    }

    val nexus = Nexus(
      baseUrl = baseUrl,
      username = repositoryUsername,
      password = repositoryPassword,
    )

    val manualStagingRepositoryId = this.manualStagingRepositoryId
    if (manualStagingRepositoryId != null) {
      nexus.closeAndReleaseRepositoryById(manualStagingRepositoryId)
    } else {
      nexus.closeAndReleaseCurrentRepository()
    }
  }
}
