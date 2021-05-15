package com.vanniktech.maven.publish.legacy

import com.vanniktech.maven.publish.AndroidLibrary
import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJs
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.baseExtension
import com.vanniktech.maven.publish.legacyExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.plugins.signing.SigningPlugin
import org.jetbrains.dokka.gradle.DokkaTask

internal fun Project.configureMavenCentral() {
  afterEvaluate {
    legacyExtension.sonatypeHost?.let { baseExtension.publishToMavenCentral(it) }
  }
}

internal fun Project.configureSigning() {
  // Apply signing immediately. It is also applied by `signAllPublications` but the afterEvaluate means
  // that it's APIs are not available for consumers without also using afterEvaluate.
  plugins.apply(SigningPlugin::class.java)

  afterEvaluate {
    if (legacyExtension.releaseSigningEnabled) {
      baseExtension.signAllPublications()
    }
  }
}

internal fun Project.configurePom() {
  baseExtension.pomFromGradleProperties()
}

internal fun Project.configurePlatform() {
  afterEvaluate {
    when {
      plugins.hasPlugin("org.jetbrains.kotlin.multiplatform") ->
        baseExtension.configure(KotlinMultiplatform(defaultJavaDocOption() ?: JavadocJar.Empty()))
      plugins.hasPlugin("org.jetbrains.kotlin.jvm") ->
        baseExtension.configure(KotlinJvm(defaultJavaDocOption() ?: javadoc()))
      plugins.hasPlugin("org.jetbrains.kotlin.js") ->
        baseExtension.configure(KotlinJs(defaultJavaDocOption() ?: JavadocJar.Empty()))
      plugins.hasPlugin("java-gradle-plugin") ->
        baseExtension.configure(GradlePlugin(defaultJavaDocOption() ?: javadoc()))
      plugins.hasPlugin("com.android.library") -> {
        val variant = legacyExtension.androidVariantToPublish
        baseExtension.configure(AndroidLibrary(defaultJavaDocOption() ?: javadoc(), variant = variant))
      }
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
    JavadocJar.Dokka(findDokkaTask())
  } else {
    null
  }
}

private fun Project.javadoc(): JavadocJar {
  tasks.withType(Javadoc::class.java).configureEach {
    val options = it.options as StandardJavadocDocletOptions
    if (JavaVersion.current().isJava9Compatible) {
      options.addBooleanOption("html5", true)
    }
    if (JavaVersion.current().isJava8Compatible) {
      options.addStringOption("Xdoclint:none", "-quiet")
    }
  }
  return JavadocJar.Javadoc()
}

private fun Project.findDokkaTask(): String {
  val tasks = project.tasks.withType(DokkaTask::class.java)
  return if (tasks.size == 1) {
    tasks.first().name
  } else {
    tasks.findByName("dokkaHtml")?.name ?: "dokka"
  }
}
