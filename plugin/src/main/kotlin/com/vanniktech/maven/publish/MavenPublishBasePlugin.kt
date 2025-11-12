package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.BuildConfig.ANDROID_GRADLE_MIN
import com.vanniktech.maven.publish.BuildConfig.KOTLIN_MIN
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
      try {
        if (!isAtLeastAgp(ANDROID_GRADLE_MIN)) {
          error("You need AGP version $ANDROID_GRADLE_MIN or newer")
        }
      } catch (t: Throwable) {
        throw IllegalStateException(
          "Make sure the AGP version $ANDROID_GRADLE_MIN or newer is applied." +
            "Otherwise, detected Android Library plugin but was not able to access AGP classes. Please make sure " +
            "that the Android plugin and the publish plugin are applied to the same project. In many cases this means " +
            "you need to add both the root project with `apply false`.",
          t,
        )
      }
    }
    KOTLIN_PLUGIN_IDS.forEach { pluginId ->
      plugins.withId(pluginId) {
        try {
          if (!isAtLeastKgp(pluginId, KOTLIN_MIN)) {
            error("You need Kotlin version $KOTLIN_MIN or newer")
          }
        } catch (t: Throwable) {
          throw IllegalStateException(
            "Make sure the Kotlin version $KOTLIN_MIN or newer is applied." +
              "Otherwise, detected Kotlin plugin $pluginId but was not able to access Kotlin plugin classes. Please make sure " +
              "that the Kotlin plugin and the publish plugin are applied to the same project. In many cases this means " +
              "you need to add both the root project with `apply false`.",
            t,
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
