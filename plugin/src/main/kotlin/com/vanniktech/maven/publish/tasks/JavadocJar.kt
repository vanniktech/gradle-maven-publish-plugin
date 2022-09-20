package com.vanniktech.maven.publish.tasks

import com.vanniktech.maven.publish.JavadocJar as JavadocJarOption
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask

open class JavadocJar : Jar() {

  init {
    archiveClassifier.set("javadoc")
  }

  internal companion object {
    internal fun Project.javadocJarTask(javadocJar: JavadocJarOption): TaskProvider<*>? {
      return when (javadocJar) {
        is JavadocJarOption.None -> null
        is JavadocJarOption.Empty -> emptyJavadocJar()
        is JavadocJarOption.Javadoc -> plainJavadocJar()
        is JavadocJarOption.Dokka -> dokkaJavadocJar()
      }
    }

    private fun Project.emptyJavadocJar(): TaskProvider<*> = tasks.register("emptyJavadocJar", JavadocJar::class.java)

    private fun Project.plainJavadocJar(): TaskProvider<*> {
      return tasks.register("simpleJavadocJar", JavadocJar::class.java) {
        val task = tasks.named("javadoc")
        it.dependsOn(task)
        it.from(task)
      }
    }

    private fun Project.dokkaJavadocJar(): TaskProvider<*> {
      return tasks.register("dokkaJavadocJar", JavadocJar::class.java) {
        val task = provider { findDokkaTask() }
        it.dependsOn(task)
        it.from(task)
      }
    }

    private fun Project.findDokkaTask(): String {
      val tasks = project.tasks.withType(DokkaTask::class.java)
      return if (tasks.size == 1) {
        tasks.first().name
      } else {
        tasks.findByName("dokkaHtml")?.name ?: "dokka"
      }
    }
  }
}
