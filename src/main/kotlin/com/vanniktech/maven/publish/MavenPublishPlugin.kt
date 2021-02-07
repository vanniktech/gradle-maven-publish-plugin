package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.legacy.configureTargets
import org.gradle.api.JavaVersion
import com.vanniktech.maven.publish.nexus.NexusConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin as GradleMavenPublishPlugin
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.plugins.signing.SigningPlugin
import org.gradle.util.VersionNumber

open class MavenPublishPlugin : Plugin<Project> {

  override fun apply(p: Project) {
    val extension = p.extensions.create("mavenPublish", MavenPublishPluginExtension::class.java, p)

    val gradleVersion = VersionNumber.parse(p.gradle.gradleVersion)
    if (gradleVersion < VersionNumber(MINIMUM_GRADLE_MAJOR, MINIMUM_GRADLE_MINOR, MINIMUM_GRADLE_MICRO, null)) {
      throw IllegalArgumentException("You need gradle version 6.6.0 or higher")
    }

    p.plugins.apply(GradleMavenPublishPlugin::class.java)

    val pom = MavenPublishPom.fromProject(p)
    p.group = pom.groupId
    p.version = pom.version
    p.configureTargets(extension)

    configureSigning(p)
    configureJavadoc(p)
    configureDokka(p)

    p.afterEvaluate { project ->
      configurePublishing(project, pom)
    }

    NexusConfigurer(p)
  }

  private fun configureSigning(project: Project) {
    project.plugins.apply(SigningPlugin::class.java)
    project.signing.setRequired(project.isSigningRequired)
    project.afterEvaluate {
      if (project.isSigningRequired.call() && project.project.publishExtension.releaseSigningEnabled) {
        @Suppress("UnstableApiUsage")
        project.signing.sign(project.publishing.publications)
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
  private fun configurePublishing(project: Project, pom: MavenPublishPom) {
    val configurer = MavenPublishConfigurer(project, pom)
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
    const val MINIMUM_GRADLE_MAJOR = 6
    const val MINIMUM_GRADLE_MINOR = 6
    const val MINIMUM_GRADLE_MICRO = 0

    const val PLUGIN_DOKKA = "org.jetbrains.dokka"
  }
}
