package com.vanniktech.maven.publish.tasks

import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask

@Suppress("UnstableApiUsage")
open class JavadocsJar : Jar() {

  init {
    archiveClassifier.set("javadoc")

    if (project.plugins.hasPlugin("org.jetbrains.dokka")) {
      val dokkaTask = project.tasks.getByName("dokka") as DokkaTask
      dependsOn(dokkaTask)
      from(dokkaTask.outputDirectory)
    } else {
      val javadocTask = project.tasks.getByName("javadoc") as Javadoc
      dependsOn(javadocTask)
      from(javadocTask.destinationDir)
    }
  }
}
