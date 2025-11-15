package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.workaround.gradlePropertyCompat
import org.gradle.api.Plugin
import org.gradle.api.Project

public abstract class MavenPublishPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.plugins.apply(MavenPublishBasePlugin::class.java)
    val baseExtension = project.baseExtension

    if (project.sonatypeHost()) {
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

  private fun Project.sonatypeHost(): Boolean {
    gradlePropertyCompat("mavenCentralPublishing").orNull?.let { central ->
      return central.toBoolean()
    }
    return when (gradlePropertyCompat("SONATYPE_HOST").orNull) {
      null -> false
      "CENTRAL_PORTAL" -> true
      else -> error(
        """
        OSSRH was shut down on June 30, 2025. Migrate to CENTRAL_PORTAL instead.
        See more info at https://central.sonatype.org/news/20250326_ossrh_sunset.
        """.trimIndent(),
      )
    }
  }

  private fun Project.automaticRelease(): Boolean {
    gradlePropertyCompat("mavenCentralAutomaticPublishing").orNull?.let { automatic ->
      return automatic.toBoolean()
    }
    return gradlePropertyCompat("SONATYPE_AUTOMATIC_RELEASE").orNull.toBoolean()
  }

  private fun Project.validateDeployment(): Boolean {
    gradlePropertyCompat("mavenCentralDeploymentValidation").orNull.let { automatic ->
      return automatic.toBoolean()
    }
    return gradlePropertyCompat("SONATYPE_DEPLOYMENT_VALIDATION").getOrElse("true").toBoolean()
  }

  private fun Project.signAllPublications(): Boolean {
    gradlePropertyCompat("signAllPublications").orNull?.let { sign ->
      return sign.toBoolean()
    }
    return gradlePropertyCompat("RELEASE_SIGNING_ENABLED").orNull.toBoolean()
  }
}
