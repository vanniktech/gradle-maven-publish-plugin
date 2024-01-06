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
  AGP_8_2(
    value = "8.2.1",
    minGradleVersion = GradleVersion.GRADLE_8_1,
    minJdkVersion = JavaVersion.VERSION_17,
  ),

  // beta channel
  AGP_8_3(
    value = "8.3.0-beta01",
    minGradleVersion = GradleVersion.GRADLE_8_1,
    minJdkVersion = JavaVersion.VERSION_17,
  ),

  // canary channel
  AGP_8_4(
    value = "8.4.0-alpha03",
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
  KT_1_9_22("1.9.22"),

  // beta
  KT_2_0_0("2.0.0-Beta2"),
  ;

  companion object {
    // aliases for skipped versions
    val KT_1_9_0 = KT_1_9_22
    val KT_1_9_20 = KT_1_9_22
  }
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
  GRADLE_8_5(
    value = "8.5",
  ),

  // rc
  GRADLE_8_6(
    value = "8.6-rc-1",
  ),
  ;

  companion object {
    // aliases for the skipped version to be able to reference the correct one in AgpVersion
    val GRADLE_8_0 = GRADLE_8_5
    val GRADLE_8_1 = GRADLE_8_5
    val GRADLE_8_2 = GRADLE_8_5
    val GRADLE_8_3 = GRADLE_8_5
    val GRADLE_8_4 = GRADLE_8_5
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
