package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.internal.isAtLeastKotlinVersion
import com.vanniktech.maven.publish.internal.isAtLeastUsingAndroidGradleVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin as GradleMavenPublishPlugin
import org.gradle.util.GradleVersion

public open class MavenPublishBasePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.plugins.apply(GradleMavenPublishPlugin::class.java)

    project.checkMinimumVersions()

    project.extensions.create("mavenPublishing", MavenPublishBaseExtension::class.java, project)
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
    val MIN_GRADLE_VERSION: GradleVersion = GradleVersion.version("8.5")
    val KOTLIN_PLUGIN_IDS = listOf(
      "org.jetbrains.kotlin.jvm",
      "org.jetbrains.kotlin.multiplatform",
    )
  }
}
