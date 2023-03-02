rootProject.name = "gradle-maven-publish-plugin"

pluginManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
  }
}

plugins {
  id("com.gradle.enterprise") version "3.12.4"
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
    publishAlways()
  }
}

include(":plugin")
include(":nexus")
includeBuild("build-logic")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
