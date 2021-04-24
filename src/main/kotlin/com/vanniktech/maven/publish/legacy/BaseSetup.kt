package com.vanniktech.maven.publish.legacy

import com.vanniktech.maven.publish.AndroidLibrary
import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJs
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.baseExtension
import com.vanniktech.maven.publish.findOptionalProperty
import com.vanniktech.maven.publish.legacyExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.jetbrains.dokka.gradle.DokkaTask

internal fun Project.configureMavenCentral() {
  afterEvaluate {
    baseExtension.publishToMavenCentral(legacyExtension.sonatypeHost)
  }
}

internal fun Project.configureSigning() {
  afterEvaluate {
    if (legacyExtension.releaseSigningEnabled) {
      baseExtension.signAllPublications()
    }
  }
}

internal fun Project.configurePom() {
  // without afterEvaluate https://github.com/gradle/gradle/issues/12259 will happen
  afterEvaluate {
    baseExtension.pom { pom ->
      val name = project.findOptionalProperty("POM_NAME")
      if (name != null) {
        pom.name.set(name)
      }
      val description = project.findOptionalProperty("POM_DESCRIPTION")
      if (description != null) {
        pom.description.set(description)
      }
      val url = project.findOptionalProperty("POM_URL")
      if (url != null) {
        pom.url.set(url)
      }
      val inceptionYear = project.findOptionalProperty("POM_INCEPTION_YEAR")
      if (inceptionYear != null) {
        pom.inceptionYear.set(inceptionYear)
      }

      val scmUrl = project.findOptionalProperty("POM_SCM_URL")
      val scmConnection = project.findOptionalProperty("POM_SCM_CONNECTION")
      val scmDeveloperConnection = project.findOptionalProperty("POM_SCM_DEV_CONNECTION")
      if (scmUrl != null || scmConnection != null || scmDeveloperConnection != null) {
        pom.scm {
          it.url.set(scmUrl)
          it.connection.set(scmConnection)
          it.developerConnection.set(scmDeveloperConnection)
        }
      }

      val licenseName = project.findOptionalProperty("POM_LICENCE_NAME")
      val licenseUrl = project.findOptionalProperty("POM_LICENCE_URL")
      val licenseDistribution = project.findOptionalProperty("POM_LICENCE_DIST")
      if (licenseName != null || licenseUrl != null || licenseDistribution != null) {
        pom.licenses { licenses ->
          licenses.license {
            it.name.set(licenseName)
            it.url.set(licenseUrl)
            it.distribution.set(licenseDistribution)
          }
        }
      }

      val developerId = project.findOptionalProperty("POM_DEVELOPER_ID")
      val developerName = project.findOptionalProperty("POM_DEVELOPER_NAME")
      val developerUrl = project.findOptionalProperty("POM_DEVELOPER_URL")
      if (developerId != null || developerName != null || developerUrl != null) {
        pom.developers { developers ->
          developers.developer {
            it.id.set(developerId)
            it.name.set(developerName)
            it.url.set(developerUrl)
          }
        }
      }
    }
  }
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
