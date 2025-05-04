package com.vanniktech.maven.publish

import org.gradle.api.Plugin
import org.gradle.api.Project

public open class MavenPublishPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.plugins.apply(MavenPublishBasePlugin::class.java)
    val baseExtension = project.baseExtension

    val sonatypeHost = project.findOptionalProperty("SONATYPE_HOST")
    if (!sonatypeHost.isNullOrBlank()) {
      val automaticRelease = project.findOptionalProperty("SONATYPE_AUTOMATIC_RELEASE").toBoolean()
      baseExtension.publishToMavenCentral(SonatypeHost.valueOf(sonatypeHost), automaticRelease)
    }
    val releaseSigning = project.findOptionalProperty("RELEASE_SIGNING_ENABLED").toBoolean()
    if (releaseSigning) {
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
}
