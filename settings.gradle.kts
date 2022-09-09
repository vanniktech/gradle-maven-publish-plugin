rootProject.name = "gradle-maven-publish-plugin"

include(":plugin")
include(":nexus")
includeBuild("build-logic")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
