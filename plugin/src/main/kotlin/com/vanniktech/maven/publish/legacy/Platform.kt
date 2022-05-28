package com.vanniktech.maven.publish.legacy

import com.vanniktech.maven.publish.AndroidLibrary
import com.vanniktech.maven.publish.AndroidMultiVariantLibrary
import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJs
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.androidComponents
import com.vanniktech.maven.publish.baseExtension
import com.vanniktech.maven.publish.isAtLeastUsingAndroidGradleVersion
import com.vanniktech.maven.publish.isAtLeastUsingAndroidGradleVersionAlpha
import com.vanniktech.maven.publish.isAtLeastUsingAndroidGradleVersionBeta
import com.vanniktech.maven.publish.legacyExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.jetbrains.dokka.gradle.DokkaTask

internal fun Project.configurePlatform() {
  plugins.withId("org.jetbrains.kotlin.multiplatform") {
    baseExtension.configure(KotlinMultiplatform(defaultJavaDocOption() ?: JavadocJar.Empty()))
  }

  plugins.withId("com.android.library") {
    configureAndroidPlatform()
  }

  afterEvaluate {
    configureNotAndroidNotMppPlatform()
  }
}

internal fun Project.configureNotAndroidNotMppPlatform() {
  when {
    plugins.hasPlugin("org.jetbrains.kotlin.multiplatform") -> return // Handled separately.
    plugins.hasPlugin("com.android.library") -> return // Handled separately.
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

internal fun Project.configureAndroidPlatform() {
  if (hasWorkingNewAndroidPublishingApi()) {
    // afterEvaluate is too late but we can't run this synchronously because we shouldn't call the APIs for
    // multiplatform projects that use Android
    androidComponents.finalizeDsl {
      if (!plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
        val variant = legacyExtension.androidVariantToPublish
        if (variant != null) {
          baseExtension.configure(AndroidSingleVariantLibrary(variant))
        } else {
          baseExtension.configure(AndroidMultiVariantLibrary())
        }
      }
    }
  } else {
    afterEvaluate {
      // release was the old default value before it was changed to null for AGP 7.1+
      val variant = legacyExtension.androidVariantToPublish ?: "release"
      baseExtension.configure(AndroidLibrary(defaultJavaDocOption() ?: javadoc(), variant = variant))
    }
  }
}

private fun Project.hasWorkingNewAndroidPublishingApi(): Boolean {
  // All 7.3.0 builds starting from 7.3.0-alpha01 are fine.
  if (isAtLeastUsingAndroidGradleVersionAlpha(7, 3, 0, 1)) {
    return true
  }
  // 7.2.0 is fine starting with beta 2
  if (isAtLeastUsingAndroidGradleVersionAlpha(7, 2, 0, 1)) {
    return isAtLeastUsingAndroidGradleVersionBeta(7, 2, 0, 2)
  }
  // Earlier versions are fine starting with 7.1.2
  return isAtLeastUsingAndroidGradleVersion(7, 1, 2)
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
      val version = toolchain.languageVersion.forUseAtConfigurationTime().get().asInt()
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
