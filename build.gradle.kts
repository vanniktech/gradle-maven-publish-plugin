plugins {
  alias(libs.plugins.buildconfig) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.android.lint) apply false
  alias(libs.plugins.spotless) apply false
}

allprojects {
  plugins.apply(rootProject.libs.plugins.spotless.get().pluginId)
  extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
      target("src/**/*.kt")
      ktfmt(libs.ktfmt.get().version).googleStyle()
    }
    kotlinGradle {
      ktfmt(libs.ktfmt.get().version).googleStyle()
    }
  }
}
