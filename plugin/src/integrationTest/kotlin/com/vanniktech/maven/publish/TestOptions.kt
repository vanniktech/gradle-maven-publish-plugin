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
  AGP_7_3(
    value = "7.3.1",
    minGradleVersion = GradleVersion.GRADLE_7_4,
  ),

  // stable
  AGP_7_4(
    value = "7.4.2",
    minGradleVersion = GradleVersion.GRADLE_7_5,
  ),

  // beta channel
  AGP_8_0(
    value = "8.0.0-beta05",
    minGradleVersion = GradleVersion.GRADLE_8_0,
    minJdkVersion = JavaVersion.VERSION_17,
  ),

  // canary channel
  AGP_8_1(
    value = "8.1.0-alpha09",
    minGradleVersion = GradleVersion.GRADLE_8_0,
    minJdkVersion = JavaVersion.VERSION_17,
  ),
}

enum class KotlinVersion(val value: String) {
  // minimum supported
  KT_1_7("1.7.0"),

  // stable
  KT_1_8("1.8.10"),

  // preview
  KT_1_8_BETA("1.8.20-RC"),
}

enum class GradleVersion(val value: String) {
  // minimum supported
  GRADLE_7_4("7.4"),

  // stable
  GRADLE_8_0("8.0.2"),

  // preview
  GRADLE_8_1("8.1-rc-1"),
  ;

  companion object {
    // aliases for the skipped version to be able to reference the correct one in AgpVersion
    val GRADLE_7_5 = GRADLE_8_0
    val GRADLE_7_6 = GRADLE_8_0
  }
}
