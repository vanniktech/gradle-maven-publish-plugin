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
  val minJdkVersion: JavaVersion = JavaVersion.VERSION_11,
) {
  // minimum supported
  AGP_7_4(
    value = "7.4.0",
    minGradleVersion = GradleVersion.GRADLE_7_6,
  ),

  // stable
  AGP_8_1(
    value = "8.1.0",
    minGradleVersion = GradleVersion.GRADLE_8_1,
    minJdkVersion = JavaVersion.VERSION_17,
  ),

  // canary channel
  AGP_8_2(
    value = "8.2.0-alpha15",
    minGradleVersion = GradleVersion.GRADLE_8_1,
    minJdkVersion = JavaVersion.VERSION_17,
  ),
}

enum class KotlinVersion(
  val value: String,
  val firstUnsupportedJdkVersion: JavaVersion? = null,
  val firstUnsupportedGradleVersion: GradleVersion? = null,
) {
  // minimum supported
  KT_1_8_20(
    "1.8.20",
    firstUnsupportedJdkVersion = JavaVersion.VERSION_20,
  ),

  // stable
  KT_1_9_0("1.9.0"),
  ;
}

enum class GradleVersion(
  val value: String,
  val firstUnsupportedJdkVersion: JavaVersion? = null,
) {
  // minimum supported
  GRADLE_7_6(
    value = "7.6",
    firstUnsupportedJdkVersion = JavaVersion.VERSION_18,
  ),

  // stable
  GRADLE_8_2(
    value = "8.2.1",
    firstUnsupportedJdkVersion = JavaVersion.VERSION_20,
  ),

  // preview
  GRADLE_8_3("8.3-rc-3"),
  ;

  companion object {
    // aliases for the skipped version to be able to reference the correct one in AgpVersion
    val GRADLE_8_0 = GRADLE_8_2
    val GRADLE_8_1 = GRADLE_8_2
  }
}

enum class GradlePluginPublish(val version: String) {
  // minimum supported
  GRADLE_PLUGIN_PUBLISH_1_0("1.0.0"),

  // stable
  GRADLE_PLUGIN_PUBLISH_1_2("1.2.0"),
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
