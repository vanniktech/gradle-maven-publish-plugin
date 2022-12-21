package com.vanniktech.maven.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin as GradleMavenPublishPlugin
import org.gradle.util.GradleVersion

open class MavenPublishBasePlugin : Plugin<Project> {

  override fun apply(project: Project) {
    if (GradleVersion.current() < MIN_GRADLE_VERSION) {
      error("You need Gradle version $MIN_GRADLE_VERSION or higher, was ${GradleVersion.current()}")
    }
    project.plugins.withId("com.android.library") {
      if (!project.hasWorkingNewAndroidPublishingApi()) {
        error("You need AGP version 7.1.2, 7.2.0-beta02, 7.3.0-alpha01 or newer")
      }
    }

    project.plugins.apply(GradleMavenPublishPlugin::class.java)

    project.extensions.create("mavenPublishing", MavenPublishBaseExtension::class.java, project)
  }

  private fun Project.hasWorkingNewAndroidPublishingApi(): Boolean {
    // All 7.3.0 builds starting from 7.3.0-alpha01 are fine.
    if (isAtLeastUsingAndroidGradleVersionAlpha(7, 3, 0, 1)) {
      return true
    }
    // 7.2.0 is fine starting with beta 2
    if (isAtLeastUsingAndroidGradleVersionAlpha(7, 2, 0, 1)) {
      return isAtLeastUsingAndroidGradleVersionBeta(7, 2, 0, 2)
    }
    // Earlier versions are fine starting with 7.1.2
    return isAtLeastUsingAndroidGradleVersion(7, 1, 2)
  }

  private companion object {
    val MIN_GRADLE_VERSION: GradleVersion = GradleVersion.version("7.3")
  }
}
