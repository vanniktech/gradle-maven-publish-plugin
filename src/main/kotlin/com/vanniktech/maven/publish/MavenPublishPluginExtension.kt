package com.vanniktech.maven.publish

import org.gradle.api.Project

/**
 * Extension for maven publish plugin.
 * @since 0.1.0
 */
open class MavenPublishPluginExtension(project: Project) {
  /**
   * The release repository url this should be published to.
   * This defaults to the sonatypes url.
   * @since 0.1.0
   */
  var releaseRepositoryUrl: String = project.findProperty("RELEASE_REPOSITORY_URL") as String? ?: System.getenv("RELEASE_REPOSITORY_URL") ?: "https://oss.sonatype.org/service/local/staging/deploy/maven2/"

  /**
   * The snapshot repository url this should be published to.
   * This defaults to the sonatypes url.
   * @since 0.1.0
   */
  var snapshotRepositoryUrl: String = project.findProperty("SNAPSHOT_REPOSITORY_URL") as String? ?: System.getenv("SNAPSHOT_REPOSITORY_URL") ?: "https://oss.sonatype.org/content/repositories/snapshots/"

  /**
   * The username that should be used for publishing.
   * This defaults to either the SONATYPE_NEXUS_USERNAME Gradle property or the system environment variable.
   * @since 0.1.0
   */
  var repositoryUsername: String? = project.findProperty("SONATYPE_NEXUS_USERNAME") as String? ?: System.getenv("SONATYPE_NEXUS_USERNAME")

  /**
   * The password that should be used for publishing.
   * This defaults to either the SONATYPE_NEXUS_PASSWORD Gradle property or the system environment variable.
   * @since 0.1.0
   */
  var repositoryPassword: String? = project.findProperty("SONATYPE_NEXUS_PASSWORD") as String? ?: System.getenv("SONATYPE_NEXUS_PASSWORD")
}
