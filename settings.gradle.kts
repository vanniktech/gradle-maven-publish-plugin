rootProject.name = "gradle-maven-publish-plugin"

pluginManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
  }
}

plugins {
  id("com.gradle.develocity") version "3.17.2"
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
  }
}

include(":plugin")
include(":nexus")
include(":central-portal")
includeBuild("build-logic")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
