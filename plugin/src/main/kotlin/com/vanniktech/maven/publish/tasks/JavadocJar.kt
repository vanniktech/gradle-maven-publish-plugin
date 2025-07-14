package com.vanniktech.maven.publish.tasks

import com.vanniktech.maven.publish.JavadocJar as JavadocJarOption
import com.vanniktech.maven.publish.JavadocJar.Dokka.DokkaTaskWrapper
import com.vanniktech.maven.publish.baseExtension
import java.util.Locale
import org.gradle.api.Project
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

@CacheableTask
public open class JavadocJar : Jar() {
  init {
    archiveClassifier.set("javadoc")
  }

  internal companion object {
    internal fun Project.javadocJarTask(javadocJar: JavadocJarOption, prefix: String?): TaskProvider<out Jar>? = when (javadocJar) {
      is JavadocJarOption.None -> null
      is JavadocJarOption.Empty -> emptyJavadocJar(prefix)
      is JavadocJarOption.Javadoc -> plainJavadocJar(prefix)
      is JavadocJarOption.Dokka -> dokkaJavadocJar(prefix, javadocJar.wrapper)
    }

    private fun Project.emptyJavadocJar(prefix: String?): TaskProvider<out Jar> = tasks.register(
      prefixedTaskName("emptyJavadocJar", prefix),
      JavadocJar::class.java,
    ) {
      it.updateArchivesBaseNameWithPrefix(project, prefix)
    }

    private fun Project.plainJavadocJar(prefix: String?): TaskProvider<out Jar> =
      tasks.register(prefixedTaskName("plainJavadocJar", prefix), JavadocJar::class.java) {
        val javadocTask = tasks.named("javadoc")
        it.dependsOn(javadocTask)
        it.from(javadocTask)
        it.updateArchivesBaseNameWithPrefix(project, prefix)
      }

    private fun Project.dokkaJavadocJar(prefix: String?, dokkaTaskWrapper: DokkaTaskWrapper): TaskProvider<out Jar> =
      tasks.register(prefixedTaskName("dokkaJavadocJar", prefix), JavadocJar::class.java) {
        val dokkaTask = dokkaTaskWrapper.asProvider(project)
        it.dependsOn(dokkaTask)
        it.from(dokkaTask)
        it.updateArchivesBaseNameWithPrefix(project, prefix)
      }

    private fun prefixedTaskName(name: String, prefix: String?): String = if (prefix != null) {
      "${prefix}${name.replaceFirstChar { it.titlecase(Locale.US) }}"
    } else {
      name
    }

    private fun Jar.updateArchivesBaseNameWithPrefix(project: Project, prefix: String?) {
      if (prefix != null) {
        archiveBaseName.set(project.baseExtension.artifactId.map { "$it-$prefix" })
      }
    }
  }
}
