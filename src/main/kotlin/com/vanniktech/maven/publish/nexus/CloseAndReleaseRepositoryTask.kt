package com.vanniktech.maven.publish.nexus

import com.vanniktech.maven.publish.MavenPublishPluginExtension
import com.vanniktech.maven.publish.findMandatoryProperty
import com.vanniktech.maven.publish.findOptionalProperty
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CloseAndReleaseRepositoryTask : DefaultTask() {

  lateinit var nexusOptions: NexusOptions

  @SuppressWarnings("unused")
  @TaskAction
  fun closeAndReleaseRepository() {
    val baseUrl = nexusOptions.baseUrl
    val stagingProfile = nexusOptions.stagingProfile
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
