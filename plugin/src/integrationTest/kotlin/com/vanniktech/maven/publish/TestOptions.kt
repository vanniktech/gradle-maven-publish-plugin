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
  AGP_7_1(
    value = "7.1.2",
    minGradleVersion = GradleVersion.GRADLE_7_2,
    firstUnsupportedGradleVersion = GradleVersion.GRADLE_8_0,
  ),
  // AGP_7_2(
  //   value = "7.2.2",
  //   minGradleVersion = GradleVersion.GRADLE_7_3,
  //   maxGradleVersion = GradleVersion.GRADLE_7_6,
  // ),
  AGP_7_3(
    value = "7.3.1",
    minGradleVersion = GradleVersion.GRADLE_7_4,
  ),
  AGP_7_4(
    value = "7.4.0-beta02",
    minGradleVersion = GradleVersion.GRADLE_7_4,
  ),
  AGP_8_0(
    value = "8.0.0-alpha05",
    minGradleVersion = GradleVersion.GRADLE_7_5,
  ),
}

enum class KotlinVersion(val value: String) {
  // KT_1_5("1.5.32"),
  // KT_1_6("1.6.21"),
  KT_1_7("1.7.20"),
}

enum class GradleVersion(val value: String) {
  GRADLE_7_2("7.2"),
  // GRADLE_7_3("7.3.3"),
  // GRADLE_7_4("7.4.2"),
  GRADLE_7_5("7.5.1"),
  GRADLE_7_6("7.6-milestone-1"),
  GRADLE_8_0("8.0-milestone-1"),
  ;

  companion object {
    // aliases for the skipped version to be able to reference the correct one in AgpVersion
    val GRADLE_7_3 = GRADLE_7_5
    val GRADLE_7_4 = GRADLE_7_5
  }
}
