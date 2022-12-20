rootProject.name = "gradle-maven-publish-plugin"

plugins {
  id("com.gradle.enterprise") version "3.12.1"
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
