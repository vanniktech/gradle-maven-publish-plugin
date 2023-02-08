package com.vanniktech.maven.publish

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
  AGP_7_1(
    value = "7.1.2",
    minGradleVersion = GradleVersion.GRADLE_7_2,
    firstUnsupportedGradleVersion = GradleVersion.GRADLE_8_0,
  ),
  // stable
  AGP_7_4(
    value = "7.4.1",
    minGradleVersion = GradleVersion.GRADLE_7_5,
  ),
  // beta channel
  AGP_8_0(
    value = "8.0.0-beta02",
    minGradleVersion = GradleVersion.GRADLE_8_0,
    minJdkVersion = JavaVersion.VERSION_17,
  ),
  // canary channel
  AGP_8_1(
    value = "8.1.0-alpha03",
    minGradleVersion = GradleVersion.GRADLE_8_0,
    minJdkVersion = JavaVersion.VERSION_17,
  ),
}

enum class KotlinVersion(val value: String) {
  // minimum supported
  KT_1_7("1.7.20"),
  // stable
  KT_1_8("1.8.10"),
  // preview
  KT_1_8_BETA("1.8.20-Beta"),
}

enum class GradleVersion(val value: String) {
  // minimum supported
  GRADLE_7_3("7.3"),
  // stable
  GRADLE_7_6("7.6"),
  // preview
  GRADLE_8_0("8.0-rc-3"),
  ;

  companion object {
    // aliases for the skipped version to be able to reference the correct one in AgpVersion
    val GRADLE_7_2 = GRADLE_7_3
    val GRADLE_7_5 = GRADLE_7_6
  }
}
