package com.vanniktech.maven.publish.tasks

import com.android.build.gradle.LibraryExtension
import org.gradle.jvm.tasks.Jar

@Suppress("UnstableApiUsage")
open class AndroidSourcesJar : Jar() {

  init {
    archiveClassifier.set("sources")

    val androidExtension = project.extensions.getByType(LibraryExtension::class.java)
    from(androidExtension.sourceSets.getByName("main").java.srcDirs)
  }
}
