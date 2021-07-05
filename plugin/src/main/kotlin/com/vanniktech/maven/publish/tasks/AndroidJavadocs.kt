package com.vanniktech.maven.publish.tasks

import com.android.build.gradle.LibraryExtension
import java.io.File
import org.gradle.api.JavaVersion
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions

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

    // Append Java 8 and Android references
    val options = options as StandardJavadocDocletOptions
    options.links("https://developer.android.com/reference")
    options.links("https://docs.oracle.com/javase/8/docs/api/")

    // Workaround for the following error when running on on JDK 9+
    // "The code being documented uses modules but the packages defined in ... are in the unnamed module."
    if (JavaVersion.current() >= JavaVersion.VERSION_1_9) {
      options.addStringOption("-release", "8")
    }
  }
}
