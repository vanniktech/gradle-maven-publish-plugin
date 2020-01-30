package com.vanniktech.maven.publish

import org.gradle.api.Project

fun Project.findMandatoryProperty(propertyName: String): String {
  val value = this.findOptionalProperty(propertyName)
  return requireNotNull(value) { "Please define \"$propertyName\" in your gradle.properties file" }
}

fun Project.findOptionalProperty(propertyName: String) = findProperty(propertyName)?.toString()

val Project.publishExtension: MavenPublishPluginExtension
  get() = project.extensions.getByType(MavenPublishPluginExtension::class.java)
