package com.vanniktech.maven.publish.tasks

import com.vanniktech.maven.publish.JavadocJar as JavadocJarOption
import com.vanniktech.maven.publish.JavadocJar.Dokka.DokkaTaskName
import com.vanniktech.maven.publish.JavadocJar.Dokka.ProviderDokkaTaskName
import com.vanniktech.maven.publish.JavadocJar.Dokka.StringDokkaTaskName
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

    private fun Project.dokkaJavadocJar(taskName: DokkaTaskName): TaskProvider<*> {
      return tasks.register("dokkaJavadocJar", JavadocJar::class.java) {
        val task = when (taskName) {
          is ProviderDokkaTaskName -> taskName.value.flatMap { name -> tasks.named(name) }
          is StringDokkaTaskName -> tasks.named(taskName.value)
        }
        it.dependsOn(task)
        it.from(task)
      }
    }
  }
}
