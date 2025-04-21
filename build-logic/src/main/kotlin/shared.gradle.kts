import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val libs = the<LibrariesForLibs>()

plugins {
  id("java-library")
  id("kotlin")
  id("kotlin-kapt")
  id("com.vanniktech.maven.publish")
  id("org.jlleitschuh.gradle.ktlint")
}

repositories {
  mavenCentral()
  google()
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType(KotlinCompile::class.java) {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_11)
    languageVersion.set(KotlinVersion.KOTLIN_1_9)
  }
}

configurations.all {
  // Pin the kotlin version
  resolutionStrategy {
    force(libs.kotlin.stdlib)
    force(libs.kotlin.stdlib.jdk8)
    force(libs.kotlin.reflect)
  }
}
