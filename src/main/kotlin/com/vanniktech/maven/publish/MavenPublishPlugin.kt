package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.legacy.MavenPublishPom
import com.vanniktech.maven.publish.legacy.configureArchivesTasks
import com.vanniktech.maven.publish.legacy.checkProperties
import com.vanniktech.maven.publish.legacy.configurePom
import com.vanniktech.maven.publish.legacy.setCoordinates
import org.gradle.api.JavaVersion
import com.vanniktech.maven.publish.nexus.NexusConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.plugins.signing.SigningPlugin

open class MavenPublishPlugin : Plugin<Project> {

  override fun apply(p: Project) {
    p.plugins.apply(MavenPublishBasePlugin::class.java)

    p.extensions.create("mavenPublish", MavenPublishPluginExtension::class.java, p)

    val pom = MavenPublishPom.fromProject(p)
    p.setCoordinates(pom)
    p.configurePom(pom)
    p.checkProperties()
    p.configureArchivesTasks()

    p.publishing.repositories.maven { repo ->
      repo.name = "mavenCentral"
      repo.setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
      repo.credentials(PasswordCredentials::class.java)

      p.afterEvaluate {
        if (it.version.toString().endsWith("SNAPSHOT")) {
          repo.setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
        }
      }
    }

    configureSigning(p)
    configureJavadoc(p)
    configureDokka(p)

    p.afterEvaluate { project ->
      configurePublishing(project)
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
