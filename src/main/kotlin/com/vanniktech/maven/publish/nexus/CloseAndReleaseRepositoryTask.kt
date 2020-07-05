package com.vanniktech.maven.publish.nexus

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CloseAndReleaseRepositoryTask : DefaultTask() {

  lateinit var nexusOptions: NexusOptions

  @SuppressWarnings("unused")
  @TaskAction
  fun closeAndReleaseRepository() {
    val baseUrl = nexusOptions.baseUrl
    val stagingProfile = nexusOptions.stagingProfile

    requireNotNull(stagingProfile) {
      "Please set a value for SONATYPE_STAGING_PROFILE"
    }

    val repositoryUsername = nexusOptions.repositoryUsername

    requireNotNull(repositoryUsername) {
      "Please set a value for SONATYPE_NEXUS_USERNAME"
    }

    val repositoryPassword = nexusOptions.repositoryPassword

    requireNotNull(repositoryPassword) {
      "Please set a value for SONATYPE_NEXUS_PASSWORD"
    }

    Nexus(repositoryUsername, repositoryPassword, stagingProfile, baseUrl).closeAndReleaseRepository()
  }
}
