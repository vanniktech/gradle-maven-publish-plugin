package com.vanniktech.maven.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.VersionNumber
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin as GradleMavenPublishPlugin

open class MavenPublishBasePlugin : Plugin<Project> {

  override fun apply(project: Project) {
    val gradleVersion = VersionNumber.parse(project.gradle.gradleVersion)
    if (gradleVersion < MIN_GRADLE_VERSION) {
      error("You need Gradle version 6.6.0 or higher")
    }

    project.plugins.apply(GradleMavenPublishPlugin::class.java)

    project.extensions.create("mavenPublishing", MavenPublishBaseExtension::class.java, project)
  }

  private companion object {
    val MIN_GRADLE_VERSION = VersionNumber.parse("6.6.0")
  }
}
