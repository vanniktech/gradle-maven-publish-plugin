import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
  id("java-library")
  id("kotlin")
  id("kotlin-kapt")
  id("org.jlleitschuh.gradle.ktlint")
  id("com.vanniktech.maven.publish")
}

ktlint {
  version.set("0.41.0")
}

repositories {
  mavenCentral()
  google()
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

configurations.all {
  // Pin the kotlin version
  resolutionStrategy {
    force(libs.kotlin.stdlib)
    force(libs.kotlin.stdlib.jdk8)
    force(libs.kotlin.reflect)
  }
}
