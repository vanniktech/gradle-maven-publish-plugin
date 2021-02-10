@file:Suppress("DEPRECATION")
package com.vanniktech.maven.publish.legacy

import com.vanniktech.maven.publish.findOptionalProperty
import org.gradle.api.Project

@Suppress("ComplexMethod")
internal fun Project.checkProperties() {
  if (findOptionalProperty("RELEASE_REPOSITORY_URL") != null) {
    throw IllegalStateException("Modifying the default repository by setting the RELEASE_REPOSITORY_URL property" +
      "was removed. Use the default Gradle APIs to configure additional targets/repositories " +
      "https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories")
  }
  if (findOptionalProperty("SNAPSHOT_REPOSITORY_URL") != null) {
    throw IllegalStateException("Modifying the default repository by setting the SNAPSHOT_REPOSITORY_URL env var" +
      "was removed. Use the default Gradle APIs to configure additional targets/repositories " +
      "https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories")
  }
  if (System.getenv("RELEASE_REPOSITORY_URL") != null) {
    throw IllegalStateException("Modifying the default repository by setting the RELEASE_REPOSITORY_URL property" +
      "was removed. Use the default Gradle APIs to configure additional targets/repositories " +
      "https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories")
  }
  if (System.getenv("SNAPSHOT_REPOSITORY_URL") != null) {
    throw IllegalStateException("Modifying the default repository by setting the SNAPSHOT_REPOSITORY_URL env var" +
      "was removed. Use the default Gradle APIs to configure additional targets/repositories " +
      "https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories")
  }
  if (findOptionalProperty("SONATYPE_NEXUS_USERNAME") != null) {
    throw IllegalStateException("The property SONATYPE_NEXUS_USERNAME was removed. " +
      "Use mavenCentralRepositoryUsername instead.")
  }
  if (System.getenv("SONATYPE_NEXUS_USERNAME") != null) {
    throw IllegalStateException("The env var SONATYPE_NEXUS_USERNAME was removed. Use the Gradle property " +
      "mavenCentralRepositoryUsername or the env var ORG_GRADLE_PROJECT_mavenCentralRepositoryUsername instead.")
  }
  if (findOptionalProperty("SONATYPE_NEXUS_PASSWORD") != null) {
    throw IllegalStateException("The property SONATYPE_NEXUS_USERNAME was removed. " +
      "Use mavenCentralRepositoryPassword instead.")
  }
  if (System.getenv("SONATYPE_NEXUS_PASSWORD") != null) {
    throw IllegalStateException("The env var SONATYPE_NEXUS_USERNAME was removed. Use the Gradle property " +
      "mavenCentralRepositoryPassword or the env var ORG_GRADLE_PROJECT_mavenCentralRepositoryPassword instead.")
  }
}
