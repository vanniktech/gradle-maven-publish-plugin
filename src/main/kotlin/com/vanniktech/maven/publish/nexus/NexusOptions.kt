package com.vanniktech.maven.publish.nexus

import com.vanniktech.maven.publish.findOptionalProperty
import org.gradle.api.tasks.Input
import org.gradle.api.Project

data class NexusOptions(
  /**
   * Base url of the REST API of the nexus instance you are using.
   * Defaults to OSSRH ("https://oss.sonatype.org/service/local/").
   * @since 0.9.0
   */
  @Input
  var baseUrl: String,

  /**
   * The groupId associated with your username.
   * Defaults to the GROUP Gradle Property.
   * @since 0.9.0
   */
  @Input
  var stagingProfile: String?,

  /**
   * The username used to access the Nexus REST API.
   * Defaults to the SONATYPE_NEXUS_USERNAME Gradle property.
   * @since 0.9.0
   */
  @Input
  var repositoryUsername: String?,

  /**
   * The username used to access the Nexus REST API.
   * Defaults to the SONATYPE_NEXUS_PASSWORD Gradle property.
   * @since 0.9.0
   */
  @Input
  var repositoryPassword: String?
) {
  companion object {
    private const val OSSRH_API_BASE_URL = "https://oss.sonatype.org/service/local/"

    fun fromProject(project: Project): NexusOptions {
      return NexusOptions(
        OSSRH_API_BASE_URL,
        project.findOptionalProperty("SONATYPE_STAGING_PROFILE") ?: project.findOptionalProperty("GROUP"),
        project.findOptionalProperty("SONATYPE_NEXUS_USERNAME") ?: System.getenv("SONATYPE_NEXUS_USERNAME"),
        project.findOptionalProperty("SONATYPE_NEXUS_PASSWORD") ?: System.getenv("SONATYPE_NEXUS_PASSWORD")
      )
    }
  }
}
