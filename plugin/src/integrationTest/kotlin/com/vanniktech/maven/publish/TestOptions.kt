package com.vanniktech.maven.publish

import com.google.common.truth.TruthJUnit.assume
import com.vanniktech.maven.publish.IntegrationTestBuildConfig as Versions
import org.gradle.api.JavaVersion

data class TestOptions(
  val config: Config,
  val signing: Signing,
  val gradleVersion: GradleVersion,
) {
  enum class Config {
    DSL,
    PROPERTIES,
    BASE,
  }

  enum class Signing {
    NO_SIGNING,
    GPG_KEY,
    IN_MEMORY_KEY,
  }
}

enum class AgpVersion(
  val value: String,
  val minJdkVersion: JavaVersion = JavaVersion.VERSION_17,
  val minGradleVersion: GradleVersion = GradleVersion.GRADLE_MIN,
  val firstUnsupportedJdkVersion: JavaVersion? = null,
  val firstUnsupportedGradleVersion: GradleVersion? = null,
) {
  // minimum supported
  AGP_MIN(
    value = "8.0.0",
    minGradleVersion = GradleVersion.GRADLE_8_1,
    firstUnsupportedGradleVersion = GradleVersion.GRADLE_RC,
  ),

  // latest versions of each type
  AGP_STABLE(Versions.ANDROID_GRADLE_STABLE, minGradleVersion = GradleVersion.GRADLE_STABLE),
  AGP_RC(Versions.ANDROID_GRADLE_RC, minGradleVersion = GradleVersion.GRADLE_STABLE),
  AGP_BETA(Versions.ANDROID_GRADLE_BETA, minGradleVersion = GradleVersion.GRADLE_STABLE),
  AGP_ALPHA(Versions.ANDROID_GRADLE_ALPHA, minGradleVersion = GradleVersion.GRADLE_STABLE),
}

enum class KotlinVersion(
  val value: String,
  val minJdkVersion: JavaVersion = JavaVersion.VERSION_11,
  val minGradleVersion: GradleVersion = GradleVersion.GRADLE_MIN,
  val firstUnsupportedJdkVersion: JavaVersion? = null,
  val firstUnsupportedGradleVersion: GradleVersion? = null,
) {
  // minimum supported
  KOTLIN_MIN("1.9.24", firstUnsupportedGradleVersion = GradleVersion.GRADLE_STABLE),

  // latest versions of each type
  KOTLIN_STABLE(Versions.KOTLIN_STABLE),
  KOTLIN_RC(Versions.KOTLIN_RC),
  KOTLIN_BETA(Versions.KOTLIN_BETA),
  KOTLIN_ALPHA(Versions.KOTLIN_ALPHA),
}

enum class GradleVersion(
  val value: String,
  val minJdkVersion: JavaVersion = JavaVersion.VERSION_1_8,
  val firstUnsupportedJdkVersion: JavaVersion? = null,
) {
  // minimum supported
  GRADLE_MIN(
    value = "8.13",
    firstUnsupportedJdkVersion = JavaVersion.VERSION_24,
  ),

  // latest versions of each type
  GRADLE_STABLE(Versions.GRADLE_STABLE, minJdkVersion = JavaVersion.VERSION_17),
  GRADLE_RC(Versions.GRADLE_RC, minJdkVersion = JavaVersion.VERSION_17),
  GRADLE_BETA(Versions.GRADLE_BETA, minJdkVersion = JavaVersion.VERSION_17),
  GRADLE_ALPHA(Versions.GRADLE_ALPHA, minJdkVersion = JavaVersion.VERSION_17),
  ;

  companion object {
    // aliases for the skipped version to be able to reference the correct one in AgpVersion or conditions
    val GRADLE_8_1 = GRADLE_MIN
  }
}

enum class GradlePluginPublish(
  val version: String,
) {
  // minimum supported
  GRADLE_PLUGIN_PUBLISH_MIN("1.0.0"),

  // latest versions of each type
  GRADLE_PLUGIN_PUBLISH_STABLE(Versions.GRADLE_PUBLISH_STABLE),
  GRADLE_PLUGIN_PUBLISH_RC(Versions.GRADLE_PUBLISH_RC),
  GRADLE_PLUGIN_PUBLISH_BETA(Versions.GRADLE_PUBLISH_BETA),
  GRADLE_PLUGIN_PUBLISH_ALPHA(Versions.GRADLE_PUBLISH_ALPHA),
}

fun GradleVersion.assumeSupportedJdkVersion() {
  assume().that(JavaVersion.current()).isAtLeast(minJdkVersion)
  if (firstUnsupportedJdkVersion != null) {
    assume().that(JavaVersion.current()).isLessThan(firstUnsupportedJdkVersion)
  }
}

fun KotlinVersion.assumeSupportedJdkAndGradleVersion(gradleVersion: GradleVersion) {
  assume().that(JavaVersion.current()).isAtLeast(minJdkVersion)
  assume().that(gradleVersion).isAtLeast(minGradleVersion)
  if (firstUnsupportedJdkVersion != null) {
    assume().that(JavaVersion.current()).isLessThan(firstUnsupportedJdkVersion)
  }
  if (firstUnsupportedGradleVersion != null) {
    assume().that(gradleVersion).isLessThan(firstUnsupportedGradleVersion)
  }
}

fun AgpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion: GradleVersion) {
  assume().that(JavaVersion.current()).isAtLeast(minJdkVersion)
  assume().that(gradleVersion).isAtLeast(minGradleVersion)
  if (firstUnsupportedJdkVersion != null) {
    assume().that(JavaVersion.current()).isLessThan(firstUnsupportedJdkVersion)
  }
  if (firstUnsupportedGradleVersion != null) {
    assume().that(gradleVersion).isLessThan(firstUnsupportedGradleVersion)
  }
}
