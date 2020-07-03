package com.vanniktech.maven.publish.tasks

import com.android.build.gradle.LibraryExtension
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import java.io.File

open class AndroidJavadocs : Javadoc() {

  init {
    val androidExtension = project.extensions.getByType(LibraryExtension::class.java)

    // Append also the classpath and files for release library variants. This fixes the javadoc warnings.
    // Got it from here - https://github.com/novoda/bintray-release/pull/39/files
    val releaseVariantCompileProvider = androidExtension.libraryVariants.toList().last().javaCompileProvider
    dependsOn(androidExtension.libraryVariants.toList().last().javaCompileProvider)
    if (!project.plugins.hasPlugin("org.jetbrains.kotlin.android")) {
      setSource(androidExtension.sourceSets.getByName("main").java.srcDirs)
    }

    isFailOnError = true
    classpath += project.files(androidExtension.getBootClasspath().joinToString(File.pathSeparator))
    // Safe to call get() here because we'ved marked this as dependent on the TaskProvider
    classpath += releaseVariantCompileProvider.get().classpath
    classpath += releaseVariantCompileProvider.get().outputs.files

    // We don't need javadoc for internals.
    exclude("**/internal/*")

    // Append Java 7, Android references and docs.
    val options = options as StandardJavadocDocletOptions
    options.links("http://docs.oracle.com/javase/7/docs/api/")
    options.links("https://developer.android.com/reference")
  }
}
