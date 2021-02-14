package com.vanniktech.maven.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.VersionNumber
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin as GradleMavenPublishPlugin

open class MavenPublishBasePlugin : Plugin<Project> {

  override fun apply(project: Project) {
    val gradleVersion = VersionNumber.parse(project.gradle.gradleVersion)
    if (gradleVersion < VersionNumber(MINIMUM_GRADLE_MAJOR, MINIMUM_GRADLE_MINOR, MINIMUM_GRADLE_MICRO, null)) {
      throw IllegalArgumentException("You need Gradle version 6.6.0 or higher")
    }

    project.plugins.apply(GradleMavenPublishPlugin::class.java)

    project.extensions.create("mavenPublishing", MavenPublishBaseExtension::class.java, project)
  }

  private companion object {
    const val MINIMUM_GRADLE_MAJOR = 6
    const val MINIMUM_GRADLE_MINOR = 6
    const val MINIMUM_GRADLE_MICRO = 0
  }
}
