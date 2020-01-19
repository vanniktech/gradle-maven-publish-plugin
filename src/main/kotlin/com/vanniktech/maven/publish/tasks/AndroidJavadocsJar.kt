package com.vanniktech.maven.publish.tasks

import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask

@Suppress("UnstableApiUsage")
open class AndroidJavadocsJar : Jar() {

  init {
    archiveClassifier.set("javadoc")

    if (project.plugins.hasPlugin("dokka")) {
      val dokkaTask = project.tasks.getByName("dokka") as DokkaTask
      dependsOn(dokkaTask)
      from(dokkaTask.outputDirectory)
    } else {
      val javadocTask = project.tasks.getByName("androidJavadocs") as Javadoc
      dependsOn(javadocTask)
      from(javadocTask.destinationDir)
    }
  }
}
