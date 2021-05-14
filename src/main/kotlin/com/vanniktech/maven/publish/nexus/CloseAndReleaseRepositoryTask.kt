package com.vanniktech.maven.publish.nexus

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

open class CloseAndReleaseRepositoryTask : DefaultTask() {

  @Nested
  lateinit var nexusOptions: NexusOptions

  @Option(option = "repository", description = "Specify which staging repository to close and release.")
  @Input
  @Optional
  var stagingRepository: String? = null

  @TaskAction
  fun closeAndReleaseRepository() {
    val baseUrl = requireNotNull(nexusOptions.baseUrl) {
      "Please set a value for nexus.baseUrl"
    }
    val stagingProfile = nexusOptions.stagingProfile
    val repositoryUsername = requireNotNull(nexusOptions.repositoryUsername) {
      "Please set a value for nexus.repositoryUsername"
    }
    val repositoryPassword = requireNotNull(nexusOptions.repositoryPassword) {
      "Please set a value for nexus.repositoryPassword"
    }

    Nexus(baseUrl, repositoryUsername, repositoryPassword, stagingProfile, stagingRepository).closeAndReleaseRepository()
  }
}
