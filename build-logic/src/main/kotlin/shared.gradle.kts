import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val libs = the<LibrariesForLibs>()

plugins {
  id("java-library")
  id("kotlin")
  id("kotlin-kapt")
  id("org.jlleitschuh.gradle.ktlint")
  id("com.vanniktech.maven.publish")
}

repositories {
  mavenCentral()
  google()
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(System.getProperty("testCiJdkVersion", "11").toInt()))
  }
}

tasks.withType(JavaCompile::class.java) {
  options.release.set(11)
}

tasks.withType(KotlinCompile::class.java) {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_11)
  }
}

configurations.configureEach {
  // Pin the kotlin version
  resolutionStrategy {
    force(libs.kotlin.stdlib)
    force(libs.kotlin.stdlib.jdk8)
    force(libs.kotlin.reflect)
  }
}
