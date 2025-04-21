package com.vanniktech.maven.publish

import com.google.common.truth.TruthJUnit.assume
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
  val minGradleVersion: GradleVersion,
  val firstUnsupportedGradleVersion: GradleVersion? = null,
  val minJdkVersion: JavaVersion = JavaVersion.VERSION_17,
) {
  // minimum supported
  AGP_8_0(
    value = "8.0.0",
    minGradleVersion = GradleVersion.GRADLE_8_1,
  ),
  // latest versions of each type
  AGP_STABLE(IntegrationTestBuildConfig.ANDROID_GRADLE_STABLE, GradleVersion.GRADLE_STABLE),
  AGP_RC(IntegrationTestBuildConfig.ANDROID_GRADLE_RC, GradleVersion.GRADLE_STABLE),
  AGP_BETA(IntegrationTestBuildConfig.ANDROID_GRADLE_BETA, GradleVersion.GRADLE_STABLE),
  AGP_ALPHA(IntegrationTestBuildConfig.ANDROID_GRADLE_ALPHA, GradleVersion.GRADLE_STABLE),
}

enum class KotlinVersion(
  val value: String,
  val firstUnsupportedJdkVersion: JavaVersion? = null,
  val firstUnsupportedGradleVersion: GradleVersion? = null,
) {
  // minimum supported
  KT_1_9_24("1.9.24"),
  // latest versions of each type
  KOTLIN_STABLE(IntegrationTestBuildConfig.KOTLIN_STABLE),
  KOTLIN_RC(IntegrationTestBuildConfig.KOTLIN_RC),
  KOTLIN_BETA(IntegrationTestBuildConfig.KOTLIN_BETA),
  KOTLIN_ALPHA(IntegrationTestBuildConfig.KOTLIN_ALPHA),
}

enum class GradleVersion(
  val value: String,
  val firstUnsupportedJdkVersion: JavaVersion? = null,
) {
  // minimum supported
  GRADLE_8_5(
    value = "8.5",
    firstUnsupportedJdkVersion = JavaVersion.VERSION_22,
  ),
  // latest versions of each type
  GRADLE_STABLE(IntegrationTestBuildConfig.GRADLE_STABLE),
  GRADLE_RC(IntegrationTestBuildConfig.GRADLE_RC),
  GRADLE_BETA(IntegrationTestBuildConfig.GRADLE_BETA),
  GRADLE_ALPHA(IntegrationTestBuildConfig.GRADLE_ALPHA),
  ;

  companion object {
    // aliases for the skipped version to be able to reference the correct one in AgpVersion or conditions
    val GRADLE_8_1 = GRADLE_8_5
    val GRADLE_8_12 = GRADLE_STABLE
  }
}

enum class GradlePluginPublish(val version: String) {
  // minimum supported
  GRADLE_PLUGIN_PUBLISH_MIN("1.0.0"),
  // latest versions of each type
  GRADLE_PLUGIN_PUBLISH_STABLE(IntegrationTestBuildConfig.GRADLE_PUBLISH_STABLE),
  GRADLE_PLUGIN_PUBLISH_RC(IntegrationTestBuildConfig.GRADLE_PUBLISH_RC),
  GRADLE_PLUGIN_PUBLISH_BETA(IntegrationTestBuildConfig.GRADLE_PUBLISH_BETA),
  GRADLE_PLUGIN_PUBLISH_ALPHA(IntegrationTestBuildConfig.GRADLE_PUBLISH_ALPHA),
}

fun GradleVersion.assumeSupportedJdkVersion() {
  if (firstUnsupportedJdkVersion != null) {
    assume().that(JavaVersion.current()).isLessThan(firstUnsupportedJdkVersion)
  }
}

fun KotlinVersion.assumeSupportedJdkAndGradleVersion(gradleVersion: GradleVersion) {
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
  if (firstUnsupportedGradleVersion != null) {
    assume().that(gradleVersion).isLessThan(firstUnsupportedGradleVersion)
  }
}
