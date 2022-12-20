package com.vanniktech.maven.publish

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

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

internal fun Project.isAtLeastUsingAndroidGradleVersion(major: Int, minor: Int, patch: Int): Boolean {
  return try {
    androidComponents.pluginVersion >= AndroidPluginVersion(major, minor, patch)
  } catch (e: NoClassDefFoundError) {
    // was added in 7.0
    false
  }
}

internal fun Project.isAtLeastUsingAndroidGradleVersionBeta(major: Int, minor: Int, patch: Int, beta: Int): Boolean {
  return try {
    androidComponents.pluginVersion >= AndroidPluginVersion(major, minor, patch).beta(beta)
  } catch (e: NoClassDefFoundError) {
    // was added in 7.0
    false
  }
}

internal fun Project.isAtLeastUsingAndroidGradleVersionAlpha(major: Int, minor: Int, patch: Int, alpha: Int): Boolean {
  return try {
    androidComponents.pluginVersion >= AndroidPluginVersion(major, minor, patch).alpha(alpha)
  } catch (e: NoClassDefFoundError) {
    // was added in 7.0
    false
  }
}
