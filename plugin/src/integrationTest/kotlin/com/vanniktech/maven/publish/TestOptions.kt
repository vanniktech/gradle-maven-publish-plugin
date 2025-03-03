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

  // stable
  AGP_8_8(
    value = "8.8.2",
    minGradleVersion = GradleVersion.GRADLE_8_9,
  ),

  // beta channel
  AGP_8_9(
    value = "8.9.0-rc01",
    minGradleVersion = GradleVersion.GRADLE_8_10,
  ),

  // canary channel
  AGP_8_10(
    value = "8.10.0-alpha07",
    minGradleVersion = GradleVersion.GRADLE_8_10,
  ),
}

enum class KotlinVersion(
  val value: String,
  val firstUnsupportedJdkVersion: JavaVersion? = null,
  val firstUnsupportedGradleVersion: GradleVersion? = null,
) {
  // minimum supported
  KT_1_9_24("1.9.24"),

  // stable
  KT_2_1_10("2.1.10"),

  // beta
  KT_2_1_20("2.1.20-RC"),
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

  // stable
  GRADLE_8_13(
    value = "8.13",
  ),
  ;

  companion object {
    // aliases for the skipped version to be able to reference the correct one in AgpVersion
    val GRADLE_8_1 = GRADLE_8_5
    val GRADLE_8_9 = GRADLE_8_13
    val GRADLE_8_10 = GRADLE_8_13
    val GRADLE_8_12 = GRADLE_8_13
  }
}

enum class GradlePluginPublish(val version: String) {
  // minimum supported
  GRADLE_PLUGIN_PUBLISH_1_0("1.0.0"),

  // stable
  GRADLE_PLUGIN_PUBLISH_1_2("1.3.1"),
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
