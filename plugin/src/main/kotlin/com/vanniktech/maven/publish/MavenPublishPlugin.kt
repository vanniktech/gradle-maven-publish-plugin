package com.vanniktech.maven.publish

import org.gradle.api.Plugin
import org.gradle.api.Project

public abstract class MavenPublishPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.plugins.apply(MavenPublishBasePlugin::class.java)
    val baseExtension = project.baseExtension

    if (project.mavenCentralPublishing()) {
      baseExtension.publishToMavenCentral(
        project.automaticRelease(),
        project.validateDeployment(),
      )
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
}
