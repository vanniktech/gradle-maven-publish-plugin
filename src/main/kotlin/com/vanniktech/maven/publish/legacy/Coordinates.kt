package com.vanniktech.maven.publish.legacy

import com.vanniktech.maven.publish.MavenPublishPom
import com.vanniktech.maven.publish.publishing
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

internal fun Project.setCoordinates(pom: MavenPublishPom) {
  group = pom.groupId
  version = pom.version

  if (pom.artifactId != project.name) {
    setArtifactId(pom.artifactId)
  }
}

/**
 * Artifact id defaults to project name which is not mutable.
 * Some created publications use derived artifact ids (e.g. library, library-jvm, library-js) so it needs to be
 * replaced instead of just set.
 */
private fun Project.setArtifactId(artifactId: String) {
  publishing.publications.withType(MavenPublication::class.java).configureEach { publication ->
    // skip the plugin marker artifact which has it's own artifact id based on the plugin id
    if (publication.name.endsWith("PluginMarkerMaven")) {
      @Suppress("LabeledExpression")
      return@configureEach
    }

    val projectName = name
    val updatedArtifactId = publication.artifactId.replace(projectName, artifactId)
    publication.artifactId = updatedArtifactId

    // in Kotlin MPP projects some publications change our manually set artifactId again
    afterEvaluate {
      publishing.publications.withType(MavenPublication::class.java).named(publication.name).configure { publication ->
        if (publication.artifactId != updatedArtifactId) {
          publication.artifactId = publication.artifactId.replace(projectName, artifactId)
        }
      }
    }
  }
}
