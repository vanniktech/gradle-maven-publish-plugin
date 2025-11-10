package com.vanniktech.maven.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin as GradleMavenPublishPlugin

public abstract class MavenPublishBasePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.plugins.apply(GradleMavenPublishPlugin::class.java)

    project.checkMinimumVersions()

    project.extensions.create("mavenPublishing", MavenPublishBaseExtension::class.java, project)
  }

  private fun Project.checkMinimumVersions() {
    plugins.withId("com.android.library") {
      if (!isAtLeastUsingAndroidGradleVersion(8, 2, 2)) {
        error("You need AGP version 8.2.2 or newer")
      }
    }
    KOTLIN_PLUGIN_IDS.forEach { pluginId ->
      plugins.withId(pluginId) {
        try {
          if (!isAtLeastKotlinVersion(pluginId, 1, 9, 20)) {
            error("You need Kotlin version 1.9.20 or newer")
          }
        } catch (_: NoClassDefFoundError) {
          error(
            "Detected Kotlin plugin $pluginId but was not able to access Kotlin plugin classes. Please make sure " +
              "that the Kotlin plugin and the publish plugin are applied to the same project. In many cases this means " +
              "you need to add both the root project with `apply false`.",
          )
        }
      }
    }
  }

  private companion object {
    val KOTLIN_PLUGIN_IDS = listOf(
      "org.jetbrains.kotlin.jvm",
      "org.jetbrains.kotlin.multiplatform",
    )
  }
}
