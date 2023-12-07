package com.vanniktech.maven.publish.tasks

import com.vanniktech.maven.publish.JavadocJar as JavadocJarOption
import com.vanniktech.maven.publish.JavadocJar.Dokka.DokkaTaskName
import com.vanniktech.maven.publish.JavadocJar.Dokka.ProviderDokkaTaskName
import com.vanniktech.maven.publish.JavadocJar.Dokka.StringDokkaTaskName
import org.gradle.api.InvalidUserDataException
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
      val javadoc = tasks.named("javadoc")

      return try {
        // This can throw an DuplicateTaskException (which is private, but extends InvalidUserDataException).
        tasks.register("simpleJavadocJar", JavadocJar::class.java) { javadocJar ->
          javadocJar.dependsOn(javadoc)
          javadocJar.from(javadoc)
        }
      } catch (e: InvalidUserDataException) {
        tasks.named("simpleJavadocJar", JavadocJar::class.java) { javadocJar ->
          javadocJar.dependsOn(javadoc)
          javadocJar.from(javadoc)
        }
      }
    }

    private fun Project.dokkaJavadocJar(taskName: DokkaTaskName): TaskProvider<*> {
      val task = when (taskName) {
        is ProviderDokkaTaskName -> taskName.value.flatMap { name -> tasks.named(name) }
        is StringDokkaTaskName -> tasks.named(taskName.value)
      }

      return try {
        // This can throw an DuplicateTaskException (which is private, but extends InvalidUserDataException).
        tasks.register("dokkaJavadocJar", JavadocJar::class.java) { javadocJar ->
          javadocJar.dependsOn(task)
          javadocJar.from(task)
        }
      } catch (e: InvalidUserDataException) {
        tasks.named("dokkaJavadocJar", JavadocJar::class.java) { javadocJar ->
          javadocJar.dependsOn(task)
          javadocJar.from(task)
        }
      }
    }
  }
}
