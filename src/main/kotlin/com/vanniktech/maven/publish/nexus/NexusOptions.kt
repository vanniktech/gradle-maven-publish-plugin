package com.vanniktech.maven.publish.nexus

import com.vanniktech.maven.publish.findMandatoryProperty
import com.vanniktech.maven.publish.findOptionalProperty
import org.gradle.api.Project

class NexusOptions(project: Project) {
  /**
   * Base url of the REST API of the nexus instance you are using.
   * Defaults to OSSRH ("https://oss.sonatype.org/service/local/").
   * @since 0.9.0
   */
  var baseUrl: String = OSSRH_API_BASE_URL

  /**
   * The groupId associated with your username.
   * Defaults to the GROUP Gradle Property.
   * @since 0.9.0
   */
  var stagingProfile: String =  project.findOptionalProperty("SONATYPE_STAGING_PROFILE") ?: project.findMandatoryProperty("GROUP")

  /**
   * The username used to access the Nexus REST API.
   * Defaults to the SONATYPE_NEXUS_USERNAME Gradle property.
   * @since 0.9.0
   */
  var repositoryUsername: String? = project.findOptionalProperty("SONATYPE_NEXUS_USERNAME") ?: System.getenv("SONATYPE_NEXUS_USERNAME")

  /**
   * The username used to access the Nexus REST API.
   * Defaults to the SONATYPE_NEXUS_PASSWORD Gradle property.
   * @since 0.9.0
   */
  var repositoryPassword: String? = project.findOptionalProperty("SONATYPE_NEXUS_PASSWORD") ?: System.getenv("SONATYPE_NEXUS_PASSWORD")

  private companion object {
    const val OSSRH_API_BASE_URL = "https://oss.sonatype.org/service/local/"
  }
}
