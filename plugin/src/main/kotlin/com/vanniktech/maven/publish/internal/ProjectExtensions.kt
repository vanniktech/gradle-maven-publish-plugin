@file:Suppress("UnstableApiUsage")

package com.vanniktech.maven.publish.internal

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.variant.AndroidComponentsExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin

@Suppress(
  "GradleProjectIsolation",
) // TODO: we can't call 'providers.gradleProperty' instead due to https://github.com/gradle/gradle/issues/23572.
internal fun Project.findOptionalProperty(propertyName: String) = findProperty(propertyName)?.toString()

internal inline val Project.baseExtension: MavenPublishBaseExtension
  get() = extensions.getByType(MavenPublishBaseExtension::class.java)

internal inline val Project.gradleSigning: SigningExtension
  get() = extensions.getByType(SigningExtension::class.java)

internal inline val Project.gradlePublishing: PublishingExtension
  get() = extensions.getByType(PublishingExtension::class.java)

internal inline val Project.androidComponents: AndroidComponentsExtension<*, *, *>
  get() = extensions.getByType(AndroidComponentsExtension::class.java)

internal fun Project.mavenPublications(action: Action<MavenPublication>) {
  gradlePublishing.publications.withType(MavenPublication::class.java).configureEach(action)
}

internal fun Project.mavenPublicationsWithoutPluginMarker(action: Action<MavenPublication>) {
  mavenPublications {
    if (!it.name.endsWith("PluginMarkerMaven")) {
      action.execute(it)
    }
  }
}

internal fun Project.javaVersion(): JavaVersion {
  try {
    val extension = extensions.findByType(JavaPluginExtension::class.java)
    if (extension != null) {
      val toolchain = extension.toolchain
      val version = toolchain.languageVersion.get().asInt()
      return JavaVersion.toVersion(version)
    }
  } catch (t: Throwable) {
    // ignore failures and fallback to java version in which Gradle is running
  }
  return JavaVersion.current()
}

internal fun Project.isAtLeastKotlinVersion(id: String, major: Int, minor: Int, patch: Int): Boolean {
  val plugin = project.plugins.getPlugin(id) as KotlinBasePlugin
  val elements = plugin.pluginVersion.takeWhile { it != '-' }.split(".")
  val kgpMajor = elements[0].toInt()
  val kgpMinor = elements[1].toInt()
  val kgpPatch = elements[2].toInt()
  return kgpMajor > major ||
    (
      kgpMajor == major &&
        (
          kgpMinor > minor ||
            (kgpMinor == minor && kgpPatch >= patch)
        )
    )
}

internal fun Project.isAtLeastUsingAndroidGradleVersion(major: Int, minor: Int, patch: Int): Boolean = try {
  androidComponents.pluginVersion >= AndroidPluginVersion(major, minor, patch)
} catch (e: NoClassDefFoundError) {
  // was added in 7.0
  false
}

/**
 * Use isolated project compatible methods of accessing the root project
 * if they are available.
 *
 * Can be removed when Gradle 8.13 is the minimum supported version.
 */
internal fun Project.rootProjectBuildDir(): Provider<Directory> {
  // TODO: remove this when Gradle 8.8/8.13 is the minimum supported version.
  return when {
    GradleVersion.current() >= SETTINGS_DIRECTORY_GRADLE_VERSION -> project.provider {
      layout.settingsDirectory.dir("build")
    }
    GradleVersion.current() >= ISOLATED_PROJECT_VIEW_GRADLE_VERSION -> project.provider {
      isolated.rootProject.projectDirectory.dir("build")
    }
    else -> {
      @Suppress("GradleProjectIsolation")
      rootProject.layout.buildDirectory
    }
  }
}

private val ISOLATED_PROJECT_VIEW_GRADLE_VERSION = GradleVersion.version("8.8-rc-1")
private val SETTINGS_DIRECTORY_GRADLE_VERSION = GradleVersion.version("8.13-rc-1")
