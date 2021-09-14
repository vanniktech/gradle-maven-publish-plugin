plugins {
  kotlin("jvm").version("1.4.32")
  `kotlin-dsl`
}

dependencies {
  implementation(kotlin("gradle-plugin"))
  implementation("org.jlleitschuh.gradle:ktlint-gradle:10.0.0")
  implementation("com.vanniktech:gradle-maven-publish-plugin:0.16.0-rc1")
}

repositories {
  mavenCentral()
  google()
  gradlePluginPortal()
}
