plugins {
  kotlin("jvm").version("1.4.32")
  `kotlin-dsl`
}

dependencies {
  implementation(kotlin("gradle-plugin"))
  implementation("com.github.ben-manes:gradle-versions-plugin:0.38.0")
  implementation("org.jlleitschuh.gradle:ktlint-gradle:10.0.0")
  implementation("com.vanniktech:gradle-maven-publish-plugin:0.20.0")
}

repositories {
  mavenCentral()
  google()
  gradlePluginPortal()
}
