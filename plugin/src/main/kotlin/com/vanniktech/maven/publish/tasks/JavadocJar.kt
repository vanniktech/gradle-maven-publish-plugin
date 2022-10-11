package com.vanniktech.maven.publish.tasks

import com.vanniktech.maven.publish.JavadocJar as JavadocJarOption
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

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
        is JavadocJarOption.Dokka -> dokkaJavadocJar(javadocJar.taskName)
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

    private fun Project.dokkaJavadocJar(taskName: Any): TaskProvider<*> {
      return tasks.register("dokkaJavadocJar", JavadocJar::class.java) {
        it.dependsOn(taskName)
        it.from(taskName)
      }
    }
  }
}
