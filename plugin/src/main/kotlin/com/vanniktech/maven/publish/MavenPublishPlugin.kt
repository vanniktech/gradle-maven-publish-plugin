package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.legacy.configurePlatform
import com.vanniktech.maven.publish.legacy.setCoordinates
import org.gradle.api.Plugin
import org.gradle.api.Project

open class MavenPublishPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    project.plugins.apply(MavenPublishBasePlugin::class.java)
    val baseExtension = project.baseExtension

    project.setCoordinates()
    project.configurePlatform()

    val sonatypeHost = project.findOptionalProperty("SONATYPE_HOST")
    if (sonatypeHost != null && sonatypeHost.isNotBlank()) {
      baseExtension.publishToMavenCentral(SonatypeHost.valueOf(sonatypeHost))
    }
    val releaseSigning = project.findOptionalProperty("RELEASE_SIGNING_ENABLED")?.toBoolean()
    if (releaseSigning == true) {
      baseExtension.signAllPublications()
    }

    baseExtension.pomFromGradleProperties()
  }
}
