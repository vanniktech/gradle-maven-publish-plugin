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
    internal fun Project.emptyJavadocJar(): TaskProvider<*> = tasks.register("emptyJavadocJar", JavadocJar::class.java)

    internal fun Project.plainJavadocJar(android: Boolean): TaskProvider<*> {
      return if (android) {
        val androidJavadoc = tasks.register("androidJavadoc", AndroidJavadocs::class.java)
        tasks.register("androidJavadocJar", JavadocJar::class.java) {
          it.dependsOn(androidJavadoc)
          it.from(androidJavadoc)
        }
      } else {
        tasks.register("simpleJavadocJar", JavadocJar::class.java) {
          val task = tasks.named("javadoc")
          it.dependsOn(task)
          it.from(task)
        }
      }
    }

    internal fun Project.dokkaJavadocJar(options: JavadocJarOption.Dokka): TaskProvider<*> {
      return tasks.register("dokkaJavadocJar", JavadocJar::class.java) {
        val task = tasks.named(options.taskName)
        it.dependsOn(task)
        it.from(task)
      }
    }
  }
}
