package com.vanniktech.maven.publish

import org.gradle.api.Plugin
import org.gradle.api.Project

public open class MavenPublishPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.plugins.apply(MavenPublishBasePlugin::class.java)
    val baseExtension = project.baseExtension

    val sonatypeHost = project.sonatypeHost()
    if (sonatypeHost != null) {
      baseExtension.publishToMavenCentral(sonatypeHost, project.automaticRelease())
    }

    if (project.signAllPublications()) {
      baseExtension.signAllPublications()
    }

    baseExtension.pomFromGradleProperties()

    // afterEvaluate is too late for AGP which doesn't allow configuration after finalizeDsl
    project.plugins.withId("com.android.library") {
      project.androidComponents.finalizeDsl {
        baseExtension.configureBasedOnAppliedPlugins()
      }
    }

    project.afterEvaluate {
      // will no-op if it was already called
      baseExtension.configureBasedOnAppliedPlugins()
    }
  }

  @Suppress("DEPRECATION")
  private fun Project.sonatypeHost(): SonatypeHost? {
    val central = providers.gradleProperty("mavenCentralPublishing").orNull
    if (central != null) {
      return if (central.toBoolean()) {
        SonatypeHost.CENTRAL_PORTAL
      } else {
        null
      }
    }
    val sonatypeHost = providers.gradleProperty("SONATYPE_HOST").getOrElse("")
    return if (!sonatypeHost.isNullOrBlank()) {
      SonatypeHost.valueOf(sonatypeHost)
    } else {
      null
    }
  }

  private fun Project.automaticRelease(): Boolean {
    val automatic = providers.gradleProperty("mavenCentralAutomaticPublishing").orNull
    if (automatic != null) {
      return automatic.toBoolean()
    }
    val sonatypeAutomatic = providers.gradleProperty("SONATYPE_AUTOMATIC_RELEASE").orNull
    if (sonatypeAutomatic != null) {
      return sonatypeAutomatic.toBoolean()
    }
    return false
  }

  private fun Project.signAllPublications(): Boolean {
    val sign = providers.gradleProperty("signAllPublications").orNull
    if (sign != null) {
      return sign.toBoolean()
    }
    val singRelease = providers.gradleProperty("RELEASE_SIGNING_ENABLED").orNull
    if (singRelease != null) {
      return singRelease.toBoolean()
    }
    return false
  }
}
