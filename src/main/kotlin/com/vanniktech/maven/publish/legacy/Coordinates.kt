package com.vanniktech.maven.publish.legacy

import com.vanniktech.maven.publish.gradlePublishing
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

internal fun Project.setCoordinates(pom: MavenPublishPom) {
  group = pom.groupId
  version = pom.version

  setArtifactId(pom.artifactId)
  afterEvaluate {
    // the initial setArtifactId runs too early for multiplatform projects
    if (plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
      setArtifactId(pom.artifactId)
    }
  }
}

/**
 * Artifact id defaults to project name which is not mutable.
 * Some created publications use derived artifact ids (e.g. library, library-jvm, library-js) so it needs to be
 * replaced instead of just set.
 */
private fun Project.setArtifactId(artifactId: String) {
  gradlePublishing.publications.withType(MavenPublication::class.java).configureEach { publication ->
    // skip the plugin marker artifact which has it's own artifact id based on the plugin id
    if (!publication.name.endsWith("PluginMarkerMaven")) {
      publication.artifactId = publication.artifactId.replace(this@setArtifactId.name, artifactId)
    }
  }
}
