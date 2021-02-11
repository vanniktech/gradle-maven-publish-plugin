@file:Suppress("DEPRECATION")
package com.vanniktech.maven.publish.legacy

import com.vanniktech.maven.publish.findOptionalProperty
import org.gradle.api.Project

@Suppress("ComplexMethod")
internal fun Project.checkProperties() {
  check(findOptionalProperty("RELEASE_REPOSITORY_URL") == null) {
    "Modifying the default repository by setting the RELEASE_REPOSITORY_URL property " +
      "was removed. Use the default Gradle APIs to configure additional targets/repositories " +
      "https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories"
  }
  check(findOptionalProperty("SNAPSHOT_REPOSITORY_URL") == null) {
    "Modifying the default repository by setting the SNAPSHOT_REPOSITORY_URL env var " +
      "was removed. Use the default Gradle APIs to configure additional targets/repositories " +
      "https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories"
  }
  check(System.getenv("RELEASE_REPOSITORY_URL") == null) {
    "Modifying the default repository by setting the RELEASE_REPOSITORY_URL property " +
      "was removed. Use the default Gradle APIs to configure additional targets/repositories " +
      "https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories"
  }
  check(System.getenv("SNAPSHOT_REPOSITORY_URL") == null) {
    "Modifying the default repository by setting the SNAPSHOT_REPOSITORY_URL env var " +
      "was removed. Use the default Gradle APIs to configure additional targets/repositories " +
      "https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories"
  }
  check(findOptionalProperty("SONATYPE_NEXUS_USERNAME") == null) {
    "The property SONATYPE_NEXUS_USERNAME was removed. Use mavenCentralRepositoryUsername instead."
  }
  check(System.getenv("SONATYPE_NEXUS_USERNAME") == null) {
    "The env var SONATYPE_NEXUS_USERNAME was removed. Use the Gradle property " +
      "mavenCentralRepositoryUsername or the env var ORG_GRADLE_PROJECT_mavenCentralRepositoryUsername instead."
  }
  check(findOptionalProperty("SONATYPE_NEXUS_PASSWORD") == null) {
    "The property SONATYPE_NEXUS_PASSWORD was removed. Use mavenCentralRepositoryPassword instead."
  }
  check(System.getenv("SONATYPE_NEXUS_PASSWORD") == null) {
    "The env var SONATYPE_NEXUS_PASSWORD was removed. Use the Gradle property " +
      "mavenCentralRepositoryPassword or the env var ORG_GRADLE_PROJECT_mavenCentralRepositoryPassword instead."
  }
}
