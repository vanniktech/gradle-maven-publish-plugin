package com.vanniktech.maven.publish.tasks

import com.vanniktech.maven.publish.findDokkaTask
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar

@Suppress("UnstableApiUsage")
open class JavadocsJar : Jar() {

  init {
    archiveClassifier.set("javadoc")

    if (project.plugins.hasPlugin("org.jetbrains.dokka")) {
      val dokkaTask = project.findDokkaTask()
      dependsOn(dokkaTask)
      from(dokkaTask)
    } else {
      val javadocTask = project.tasks.getByName("javadoc") as Javadoc
      dependsOn(javadocTask)
      from(javadocTask)
    }
  }
}
