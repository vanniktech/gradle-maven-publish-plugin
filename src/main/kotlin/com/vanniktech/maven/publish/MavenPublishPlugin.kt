package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.legacy.checkProperties
import com.vanniktech.maven.publish.legacy.configureArchivesTasks
import com.vanniktech.maven.publish.legacy.configureMavenCentral
import com.vanniktech.maven.publish.legacy.configurePlatform
import com.vanniktech.maven.publish.legacy.configurePom
import com.vanniktech.maven.publish.legacy.configureSigning
import com.vanniktech.maven.publish.legacy.setCoordinates
import org.gradle.api.Plugin
import org.gradle.api.Project

open class MavenPublishPlugin : Plugin<Project> {

  override fun apply(p: Project) {
    p.plugins.apply(MavenPublishBasePlugin::class.java)

    p.extensions.create("mavenPublish", MavenPublishPluginExtension::class.java, p)

    p.setCoordinates()
    p.configurePom()
    p.checkProperties()
    p.configureMavenCentral()
    p.configureSigning()
    p.configureArchivesTasks()
    p.configurePlatform()
  }
}
