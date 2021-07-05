plugins {
  id("java-library")
  id("kotlin")
  id("kotlin-kapt")
  id("com.github.ben-manes.versions")
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


java.sourceCompatibility = JavaVersion.VERSION_1_7

// Use the kotlin version from the stdlib
val kotlinVersion = KotlinVersion.CURRENT.toString()

configurations.all {
  resolutionStrategy {
    force("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    force("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
  }
}

