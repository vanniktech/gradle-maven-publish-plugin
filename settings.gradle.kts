pluginManagement {
  repositories {
    gradlePluginPortal()
  }
  @Suppress("UnstableApiUsage")
  includeBuild("build-logic")
}
include(":plugin")
include(":nexus")
