package com.vanniktech.maven.publish.tasks

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

open class SourcesJar : Jar() {

  init {
    archiveClassifier.set("sources")
  }

  internal companion object {
    internal fun Project.emptySourcesJar(): TaskProvider<*> = tasks.register("emptySourcesJar", SourcesJar::class.java)

    internal fun Project.androidSourcesJar(sourcesJar: Boolean): TaskProvider<*> {
      if (!sourcesJar) {
        return emptySourcesJar()
      }

      return tasks.register("androidSourcesJar", SourcesJar::class.java) {
        val androidExtension = extensions.getByType(LibraryExtension::class.java)
        it.from(androidExtension.sourceSets.getByName("main").java.srcDirs)
      }
    }

    internal fun Project.javaSourcesJar(sourcesJar: Boolean): TaskProvider<*> {
      if (!sourcesJar) {
        return emptySourcesJar()
      }

      return tasks.register("javaSourcesJar", SourcesJar::class.java) {
        val javaPlugin = convention.getPlugin(JavaPluginConvention::class.java)
        it.from(javaPlugin.sourceSets.getByName("main").allSource)
      }
    }

    internal fun Project.kotlinSourcesJar(sourcesJar: Boolean): TaskProvider<*> {
      if (!sourcesJar) {
        return emptySourcesJar()
      }

      return project.tasks.named("kotlinSourcesJar")
    }
  }
}
