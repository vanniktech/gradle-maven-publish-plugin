package com.vanniktech.maven.publish.tasks

import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.jvm.tasks.Jar

@Suppress("UnstableApiUsage")
open class GroovydocsJar : Jar() {

  init {
    archiveClassifier.set("groovydoc")

    val groovydocTask = project.tasks.getByName("groovydoc") as Groovydoc
    dependsOn(groovydocTask)
    from(groovydocTask.destinationDir)
  }
}
