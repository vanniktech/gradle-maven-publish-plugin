package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.internal.androidComponents
import com.vanniktech.maven.publish.internal.baseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public open class MavenPublishPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.plugins.apply(MavenPublishBasePlugin::class.java)
    val baseExtension = project.baseExtension

    if (project.sonatypeHost()) {
      baseExtension.publishToMavenCentral(project.automaticRelease())
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
    project.plugins.withId("com.android.fused-library") {
      baseExtension.configureBasedOnAppliedPlugins()
    }

    project.afterEvaluate {
      // will no-op if it was already called
      baseExtension.configureBasedOnAppliedPlugins()
    }
  }

  private fun Project.sonatypeHost(): Boolean {
    val central = providers.gradleProperty("mavenCentralPublishing").orNull
    if (central != null) {
      return central.toBoolean()
    }
    return providers.gradleProperty("SONATYPE_HOST").orNull == "CENTRAL_PORTAL"
  }

  private fun Project.automaticRelease(): Boolean {
    val automatic = providers.gradleProperty("mavenCentralAutomaticPublishing").orNull
    if (automatic != null) {
      return automatic.toBoolean()
    }
    return providers.gradleProperty("SONATYPE_AUTOMATIC_RELEASE").orNull.toBoolean()
  }

  private fun Project.signAllPublications(): Boolean {
    val sign = providers.gradleProperty("signAllPublications").orNull
    if (sign != null) {
      return sign.toBoolean()
    }
    return providers.gradleProperty("RELEASE_SIGNING_ENABLED").orNull.toBoolean()
  }
}
