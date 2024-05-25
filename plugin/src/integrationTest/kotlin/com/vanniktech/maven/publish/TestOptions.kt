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
  AGP_8_4(
    value = "8.4.1",
    minGradleVersion = GradleVersion.GRADLE_8_6,
  ),

  // canary channel
  AGP_8_5(
    value = "8.5.0-beta02",
    minGradleVersion = GradleVersion.GRADLE_8_6,
  ),

  // canary channel
  AGP_8_6(
    value = "8.6.0-alpha03",
    minGradleVersion = GradleVersion.GRADLE_8_6,
  ),
}

enum class KotlinVersion(
  val value: String,
  val firstUnsupportedJdkVersion: JavaVersion? = null,
  val firstUnsupportedGradleVersion: GradleVersion? = null,
) {
  // minimum supported
  // stable
  KT_1_9_23("1.9.24"),

  // beta
  KT_2_0_0("2.0.0"),
}

enum class GradleVersion(
  val value: String,
  val firstUnsupportedJdkVersion: JavaVersion? = null,
) {
  // minimum supported
  GRADLE_8_1(
    value = "8.1",
    firstUnsupportedJdkVersion = JavaVersion.VERSION_20,
  ),

  // stable
  GRADLE_8_7(
    value = "8.7",
  ),

  // rc
  GRADLE_8_8(
    value = "8.8-rc-1",
  ),
  ;

  companion object {
    // aliases for the skipped version to be able to reference the correct one in AgpVersion
    val GRADLE_8_4 = GRADLE_8_7
    val GRADLE_8_6 = GRADLE_8_7
  }
}

enum class GradlePluginPublish(val version: String) {
  // minimum supported
  GRADLE_PLUGIN_PUBLISH_1_0("1.0.0"),

  // stable
  GRADLE_PLUGIN_PUBLISH_1_2("1.2.1"),
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
