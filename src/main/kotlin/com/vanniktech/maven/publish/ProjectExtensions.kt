package com.vanniktech.maven.publish

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.plugins.signing.SigningExtension
import java.util.concurrent.Callable

internal fun Project.findMandatoryProperty(propertyName: String): String {
  val value = this.findOptionalProperty(propertyName)
  return requireNotNull(value) { "Please define \"$propertyName\" in your gradle.properties file" }
}

internal fun Project.findOptionalProperty(propertyName: String) = findProperty(propertyName)?.toString()

internal inline val Project.publishExtension: MavenPublishPluginExtension
  get() = project.extensions.getByType(MavenPublishPluginExtension::class.java)

internal inline val Project.signing: SigningExtension
  get() = extensions.getByType(SigningExtension::class.java)

internal inline val Project.publishing: PublishingExtension
  get() = extensions.getByType(PublishingExtension::class.java)

internal inline val Project.isSigningRequired: Callable<Boolean>
  get() = Callable<Boolean> { !project.version.toString().contains("SNAPSHOT") }
