package com.vanniktech.maven.publish.tasks

import com.vanniktech.maven.publish.JavadocJar as JavadocJarOption
import com.vanniktech.maven.publish.JavadocJar.Dokka.DokkaTaskWrapper
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

public open class JavadocJar : Jar() {
  init {
    archiveClassifier.set("javadoc")
  }

  internal companion object {
    internal fun Project.javadocJarTask(prefix: String, javadocJar: JavadocJarOption): TaskProvider<out Jar>? = when (javadocJar) {
      is JavadocJarOption.None -> null
      is JavadocJarOption.Empty -> emptyJavadocJar(prefix)
      is JavadocJarOption.Javadoc -> plainJavadocJar(prefix)
      is JavadocJarOption.Dokka -> dokkaJavadocJar(prefix, javadocJar.wrapper)
    }

    private fun Project.emptyJavadocJar(prefix: String): TaskProvider<out Jar> = tasks.register(
      "${prefix}EmptyJavadocJar",
      JavadocJar::class.java,
    ) {
      it.archiveBaseName.set("${project.name}-$prefix-javadoc")
    }

    private fun Project.plainJavadocJar(prefix: String): TaskProvider<out Jar> =
      tasks.register("${prefix}PlainJavadocJar", JavadocJar::class.java) {
        val task = tasks.named("javadoc")
        it.dependsOn(task)
        it.from(task)
        it.archiveBaseName.set("${project.name}-$prefix-javadoc")
      }

    private fun Project.dokkaJavadocJar(prefix: String, dokkaTaskWrapper: DokkaTaskWrapper): TaskProvider<out Jar> =
      tasks.register("${prefix}DokkaJavadocJar", JavadocJar::class.java) {
        val dokkaTask = dokkaTaskWrapper.asProvider(project)
        it.dependsOn(dokkaTask)
        it.from(dokkaTask)
        it.archiveBaseName.set("${project.name}-$prefix-javadoc")
      }
  }
}
