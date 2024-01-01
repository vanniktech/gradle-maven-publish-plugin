package com.vanniktech.maven.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin as GradleMavenPublishPlugin
import org.gradle.util.GradleVersion

open class MavenPublishBasePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.plugins.apply(GradleMavenPublishPlugin::class.java)

    project.extensions.create("mavenPublishing", MavenPublishBaseExtension::class.java, project)

    project.checkMinimumVersions()
  }

  private fun Project.checkMinimumVersions() {
    if (GradleVersion.current() < MIN_GRADLE_VERSION) {
      error("You need Gradle version $MIN_GRADLE_VERSION or higher, was ${GradleVersion.current()}")
    }
    plugins.withId("com.android.library") {
      if (!isAtLeastUsingAndroidGradleVersion(8, 0, 0)) {
        error("You need AGP version 8.0.0 or newer")
      }
    }
    KOTLIN_PLUGIN_IDS.forEach { pluginId ->
      plugins.withId(pluginId) {
        if (!isAtLeastKotlinVersion(pluginId, 1, 9, 20)) {
          error("You need Kotlin version 1.9.20 or newer")
        }
      }
    }
  }

  private companion object {
    val MIN_GRADLE_VERSION: GradleVersion = GradleVersion.version("8.1")
    val KOTLIN_PLUGIN_IDS = listOf(
      "org.jetbrains.kotlin.jvm",
      "org.jetbrains.kotlin.multiplatform",
    )
  }
}
