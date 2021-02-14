package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.legacy.configureArchivesTasks
import com.vanniktech.maven.publish.legacy.checkProperties
import com.vanniktech.maven.publish.legacy.configureMavenCentral
import com.vanniktech.maven.publish.legacy.configurePom
import com.vanniktech.maven.publish.legacy.setCoordinates
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.plugins.signing.SigningPlugin

open class MavenPublishPlugin : Plugin<Project> {

  override fun apply(p: Project) {
    p.plugins.apply(MavenPublishBasePlugin::class.java)

    p.extensions.create("mavenPublish", MavenPublishPluginExtension::class.java, p)

    p.setCoordinates()
    p.configurePom()
    p.checkProperties()
    p.configureMavenCentral()
    p.configureArchivesTasks()

    configureSigning(p)
    configureJavadoc(p)
    configureDokka(p)

    p.afterEvaluate { project ->
      configurePublishing(project)
    }
  }

  private fun configureSigning(project: Project) {
    project.plugins.apply(SigningPlugin::class.java)
    project.gradleSigning.setRequired(project.isSigningRequired)
    project.afterEvaluate {
      if (project.isSigningRequired.call() && project.project.legacyExtension.releaseSigningEnabled) {
        @Suppress("UnstableApiUsage")
        project.gradleSigning.sign(project.gradlePublishing.publications)
      }
    }
  }

  private fun configureJavadoc(project: Project) {
    project.tasks.withType(Javadoc::class.java).configureEach {
      val options = it.options as StandardJavadocDocletOptions
      if (JavaVersion.current().isJava9Compatible) {
        options.addBooleanOption("html5", true)
      }
      if (JavaVersion.current().isJava8Compatible) {
        options.addStringOption("Xdoclint:none", "-quiet")
      }
    }
  }

  private fun configureDokka(project: Project) {
    project.plugins.withId("org.jetbrains.kotlin.jvm") {
      project.plugins.apply(PLUGIN_DOKKA)
    }
    project.plugins.withId("org.jetbrains.kotlin.android") {
      project.plugins.apply(PLUGIN_DOKKA)
    }
  }

  @Suppress("Detekt.ComplexMethod")
  private fun configurePublishing(project: Project) {
    val configurer = MavenPublishConfigurer(project)
    when {
      project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform") -> configurer.configureKotlinMppProject()
      project.plugins.hasPlugin("java-gradle-plugin") -> configurer.configureGradlePluginProject()
      project.plugins.hasPlugin("com.android.library") -> configurer.configureAndroidArtifacts()
      project.plugins.hasPlugin("java") || project.plugins.hasPlugin("java-library") -> configurer.configureJavaArtifacts()
      project.plugins.hasPlugin("org.jetbrains.kotlin.js") -> configurer.configureKotlinJsProject()
      else -> project.logger.warn("No compatible plugin found in project ${project.name} for publishing")
    }
  }

  companion object {
    const val PLUGIN_DOKKA = "org.jetbrains.dokka"
  }
}
