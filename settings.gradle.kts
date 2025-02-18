rootProject.name = "gradle-maven-publish-plugin"

pluginManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
  }
}

plugins {
  id("com.gradle.develocity") version "3.19.2"
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
    // TODO: workaround for https://github.com/gradle/gradle/issues/22879.
    val isCI = providers.environmentVariable("CI").isPresent
    publishing.onlyIf { isCI }
  }
}

include(":plugin")
include(":nexus")
include(":central-portal")
includeBuild("build-logic")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
