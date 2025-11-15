import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val libs = the<LibrariesForLibs>()

plugins {
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.dokka")
  id("com.vanniktech.maven.publish")
}

kotlin {
  explicitApi()
  @OptIn(ExperimentalAbiValidation::class)
  abiValidation {
    enabled = true
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release = libs.versions.jdkRelease
    .get()
    .toInt()
}

tasks.withType(KotlinCompile::class.java) {
  compilerOptions {
    // https://docs.gradle.org/current/userguide/compatibility.html#kotlin
    languageVersion.set(KotlinVersion.KOTLIN_2_2)
    jvmTarget.set(JvmTarget.fromTarget(libs.versions.jdkRelease.get()))
    freeCompilerArgs.add("-Xjdk-release=${libs.versions.jdkRelease.get()}")
  }
}

tasks.check {
  dependsOn(
    // TODO: https://youtrack.jetbrains.com/issue/KT-78525
    tasks.checkLegacyAbi,
  )
}

configurations.all {
  // Pin the kotlin version
  resolutionStrategy {
    force(libs.kotlin.stdlib)
    force(libs.kotlin.stdlib.jdk8)
    force(libs.kotlin.reflect)
  }
}
