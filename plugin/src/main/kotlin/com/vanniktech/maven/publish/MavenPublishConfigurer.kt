package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.tasks.JavadocJar.Companion.dokkaJavadocJar
import com.vanniktech.maven.publish.tasks.JavadocJar.Companion.emptyJavadocJar
import com.vanniktech.maven.publish.tasks.JavadocJar.Companion.plainJavadocJar
import com.vanniktech.maven.publish.tasks.SourcesJar.Companion.androidSourcesJar
import com.vanniktech.maven.publish.tasks.SourcesJar.Companion.emptySourcesJar
import com.vanniktech.maven.publish.tasks.SourcesJar.Companion.javaSourcesJar
import com.vanniktech.maven.publish.tasks.SourcesJar.Companion.kotlinSourcesJar
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider

internal class MavenPublishConfigurer(
  private val project: Project
) {

  fun configureGradlePluginProject(sourcesJar: Boolean, javadocJar: JavadocJar) {
    val javadocJarTask = javadocJarTask(javadocJar)

    project.gradlePublishing.publications.withType(MavenPublication::class.java).all {
      if (it.name == "pluginMaven") {
        it.withSourcesJar(sourcesJar) { project.javaSourcesJar() }
        it.withJavadocJar(javadocJarTask)
      }
    }
  }

  fun configureKotlinMppProject(javadocJar: JavadocJar) {
    val javadocJarTask = javadocJarTask(javadocJar)

    project.gradlePublishing.publications.withType(MavenPublication::class.java).all {
      it.withJavadocJar(javadocJarTask)

      // On Kotlin versions before 1.4.30 sources jars are only created for platforms, not the common artifact.
      if (it.name == "kotlinMultiplatform") {
        val sourceArtifact = it.artifacts.find { artifact -> artifact.classifier == "sources" }
        if (sourceArtifact == null) {
          it.withSourcesJar(true) { project.emptySourcesJar() }
        }
      }
    }
  }

  fun configureKotlinJsProject(sourcesJar: Boolean, javadocJar: JavadocJar) {
    val javadocJarTask = javadocJarTask(javadocJar)

    // Create publication, since Kotlin/JS doesn't provide one by default.
    // https://youtrack.jetbrains.com/issue/KT-41582
    project.gradlePublishing.publications.create("mavenJs", MavenPublication::class.java) {
      it.from(project.components.getByName("kotlin"))
      it.withSourcesJar(sourcesJar) { project.kotlinSourcesJar() }
      it.withJavadocJar(javadocJarTask)
    }
  }

  fun configureAndroidArtifacts(variant: String, sourcesJar: Boolean, javadocJar: JavadocJar) {
    val javadocJarTask = javadocJarTask(javadocJar, android = true)
    val sourcesJarTask = if (sourcesJar) {
      project.androidSourcesJar()
    } else {
      null
    }

    project.afterEvaluate {
      val component = project.components.findByName(variant) ?: throw MissingVariantException(variant)
      project.gradlePublishing.publications.create(PUBLICATION_NAME, MavenPublication::class.java) {
        it.from(component)
        it.withSourcesJar(sourcesJar) { sourcesJarTask!! }
        it.withJavadocJar(javadocJarTask)
      }
    }
  }

  fun configureJavaArtifacts(sourcesJar: Boolean, javadocJar: JavadocJar) {
    val javadocJarTask = javadocJarTask(javadocJar)

    project.gradlePublishing.publications.create(PUBLICATION_NAME, MavenPublication::class.java) {
      it.from(project.components.getByName("java"))
      it.withSourcesJar(sourcesJar) { project.javaSourcesJar() }
      it.withJavadocJar(javadocJarTask)
    }
  }

  private fun MavenPublication.withSourcesJar(sourcesJar: Boolean, factory: () -> TaskProvider<*>) {
    if (sourcesJar) {
      artifact(factory())
    } else {
      artifact(project.emptySourcesJar())
    }
  }

  private fun MavenPublication.withJavadocJar(task: TaskProvider<*>?) {
    if (task != null) {
      artifact(task)
    }
  }

  private fun javadocJarTask(javadocJar: JavadocJar, android: Boolean = false): TaskProvider<*>? {
    return when (javadocJar) {
      is JavadocJar.None -> null
      is JavadocJar.Empty -> project.emptyJavadocJar()
      is JavadocJar.Javadoc -> project.plainJavadocJar(android)
      is JavadocJar.Dokka -> project.dokkaJavadocJar(javadocJar)
    }
  }

  internal class MissingVariantException(name: String) : RuntimeException(
    "Invalid MavenPublish Configuration. Unable to find variant to publish named $name." +
      " Try setting the 'androidVariantToPublish' property in the mavenPublish" +
      " extension object to something that matches the variant that ought to be published."
  )

  companion object {
    const val PUBLICATION_NAME = "maven"
  }
}
