package com.vanniktech.maven.publish

data class TestOptions(
  val config: Config,
  val signing: Signing,
  val gradleVersion: GradleVersion,
) {
  enum class Config {
    DSL,
    PROPERTIES,
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
) {
  // minimum supported
  AGP_7_1(
    value = "7.1.2",
    minGradleVersion = GradleVersion.GRADLE_7_2,
    firstUnsupportedGradleVersion = GradleVersion.GRADLE_8_0,
  ),
  // stable
  AGP_7_3(
    value = "7.3.1",
    minGradleVersion = GradleVersion.GRADLE_7_4,
  ),
  // beta channel
  AGP_7_4(
    value = "7.4.0-rc01",
    minGradleVersion = GradleVersion.GRADLE_7_5,
  ),
  // canary channel
  AGP_8_0(
    value = "8.0.0-alpha09",
    minGradleVersion = GradleVersion.GRADLE_8_0,
  ),
}

enum class KotlinVersion(val value: String) {
  // stable
  KT_1_7("1.7.20"),
  // preview
  KT_1_8("1.8.0-RC2"),
}

enum class GradleVersion(val value: String) {
  // minimum supported
  GRADLE_7_3("7.3"),
  // stable
  GRADLE_7_6("7.6"),
  // preview
  GRADLE_8_0("8.0-milestone-6"),
  ;

  companion object {
    // aliases for the skipped version to be able to reference the correct one in AgpVersion
    val GRADLE_7_2 = GRADLE_7_3
    val GRADLE_7_4 = GRADLE_7_6
    val GRADLE_7_5 = GRADLE_7_6
  }
}
