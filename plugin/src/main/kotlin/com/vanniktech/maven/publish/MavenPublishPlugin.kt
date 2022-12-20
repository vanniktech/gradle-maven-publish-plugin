package com.vanniktech.maven.publish

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.jetbrains.dokka.gradle.DokkaTask

open class MavenPublishPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    project.plugins.apply(MavenPublishBasePlugin::class.java)
    val baseExtension = project.baseExtension

    project.setCoordinates()

    val sonatypeHost = project.findOptionalProperty("SONATYPE_HOST")
    if (!sonatypeHost.isNullOrBlank()) {
      val automaticRelease = project.findOptionalProperty("SONATYPE_AUTOMATIC_RELEASE").toBoolean()
      baseExtension.publishToMavenCentral(SonatypeHost.valueOf(sonatypeHost), automaticRelease)
    }
    val releaseSigning = project.findOptionalProperty("RELEASE_SIGNING_ENABLED")?.toBoolean()
    if (releaseSigning == true) {
      baseExtension.signAllPublications()
    }

    baseExtension.pomFromGradleProperties()

    project.configurePlatform()
  }
}

private fun Project.setCoordinates() {
  group = project.findOptionalProperty("GROUP") ?: group
  version = project.findOptionalProperty("VERSION_NAME") ?: version

  // Artifact id defaults to project name which is not mutable.
  // Some created publications use derived artifact ids (e.g. library, library-jvm, library-js) so it needs to be
  // replaced instead of just set.
  val artifactId = project.findOptionalProperty("POM_ARTIFACT_ID")
  if (artifactId != null && artifactId != project.name) {
    mavenPublicationsWithoutPluginMarker { publication ->
      val projectName = name
      val updatedArtifactId = publication.artifactId.replace(projectName, artifactId)
      publication.artifactId = updatedArtifactId

      // in Kotlin MPP projects some publications change our manually set artifactId again
      afterEvaluate {
        gradlePublishing.publications.withType(MavenPublication::class.java).named(publication.name).configure { publication ->
          if (publication.artifactId != updatedArtifactId) {
            publication.artifactId = publication.artifactId.replace(projectName, artifactId)
          }
        }
      }
    }
  }
}

private fun Project.configurePlatform() {
  plugins.withId("org.jetbrains.kotlin.multiplatform") {
    baseExtension.configure(KotlinMultiplatform(defaultJavaDocOption() ?: JavadocJar.Empty()))
  }

  plugins.withId("com.android.library") {
    // afterEvaluate is too late but we can't run this synchronously because we shouldn't call the APIs for
    // multiplatform projects that use Android
    androidComponents.finalizeDsl {
      if (!plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
        val variant = project.findOptionalProperty("ANDROID_VARIANT_TO_PUBLISH") ?: "release"
        baseExtension.configure(AndroidSingleVariantLibrary(variant))
      }
    }
  }

  afterEvaluate {
    when {
      plugins.hasPlugin("org.jetbrains.kotlin.multiplatform") -> {} // Handled above.
      plugins.hasPlugin("com.android.library") -> {} // Handled above.
      plugins.hasPlugin("java-gradle-plugin") ->
        baseExtension.configure(GradlePlugin(defaultJavaDocOption() ?: javadoc()))
      plugins.hasPlugin("org.jetbrains.kotlin.jvm") ->
        baseExtension.configure(KotlinJvm(defaultJavaDocOption() ?: javadoc()))
      plugins.hasPlugin("org.jetbrains.kotlin.js") ->
        baseExtension.configure(KotlinJs(defaultJavaDocOption() ?: JavadocJar.Empty()))
      plugins.hasPlugin("java-library") ->
        baseExtension.configure(JavaLibrary(defaultJavaDocOption() ?: javadoc()))
      plugins.hasPlugin("java") ->
        baseExtension.configure(JavaLibrary(defaultJavaDocOption() ?: javadoc()))
      else -> logger.warn("No compatible plugin found in project $name for publishing")
    }
  }
}

private fun Project.defaultJavaDocOption(): JavadocJar? {
  return if (plugins.hasPlugin("org.jetbrains.dokka") || plugins.hasPlugin("org.jetbrains.dokka-android")) {
    JavadocJar.Dokka(provider { findDokkaTask() })
  } else {
    null
  }
}

private fun Project.javadoc(): JavadocJar {
  tasks.withType(Javadoc::class.java).configureEach {
    val options = it.options as StandardJavadocDocletOptions
    val javaVersion = javaVersion()
    if (javaVersion.isJava9Compatible) {
      options.addBooleanOption("html5", true)
    }
    if (javaVersion.isJava8Compatible) {
      options.addStringOption("Xdoclint:none", "-quiet")
    }
  }
  return JavadocJar.Javadoc()
}

private fun Project.javaVersion(): JavaVersion {
  try {
    val extension = project.extensions.findByType(JavaPluginExtension::class.java)
    if (extension != null) {
      val toolchain = extension.toolchain
      val version = toolchain.languageVersion.get().asInt()
      return JavaVersion.toVersion(version)
    }
  } catch (t: Throwable) {
    // ignore failures and fallback to java version in which Gradle is running
  }
  return JavaVersion.current()
}

private fun Project.findDokkaTask(): String {
  val tasks = project.tasks.withType(DokkaTask::class.java)
  return if (tasks.size == 1) {
    tasks.first().name
  } else {
    tasks.findByName("dokkaHtml")?.name ?: "dokka"
  }
}
