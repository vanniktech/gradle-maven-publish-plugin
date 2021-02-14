package com.vanniktech.maven.publish

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.DokkaTask
import java.util.concurrent.Callable

internal fun Project.findOptionalProperty(propertyName: String) = findProperty(propertyName)?.toString()

internal inline val Project.legacyExtension: MavenPublishPluginExtension
  get() = project.extensions.getByType(MavenPublishPluginExtension::class.java)

internal inline val Project.baseExtension: MavenPublishBaseExtension
  get() = project.extensions.getByType(MavenPublishBaseExtension::class.java)

internal inline val Project.gradleSigning: SigningExtension
  get() = extensions.getByType(SigningExtension::class.java)

internal inline val Project.gradlePublishing: PublishingExtension
  get() = extensions.getByType(PublishingExtension::class.java)

internal inline val Project.isSigningRequired: Callable<Boolean>
  get() = Callable { !project.version.toString().contains("SNAPSHOT") }

internal fun Project.findDokkaTask(): DokkaTask {
  val tasks = project.tasks.withType(DokkaTask::class.java)
  return if (tasks.size == 1) {
    tasks.first()
  } else {
    tasks.findByName("dokkaHtml") ?: tasks.getByName("dokka")
  }
}
