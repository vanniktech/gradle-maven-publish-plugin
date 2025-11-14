package com.vanniktech.maven.publish

import com.google.common.truth.TruthJUnit.assume
import com.vanniktech.maven.publish.IntegrationTestBuildConfig as Versions
import net.swiftzer.semver.SemVer
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

private sealed interface ComparableVersion : Comparable<ComparableVersion> {
  val value: String
  val semVer: SemVer get() = SemVer.parse(value)

  override fun compareTo(other: ComparableVersion): Int = semVer.compareTo(other.semVer)
}

enum class AgpVersion(
  override val value: String,
  val minJdkVersion: JavaVersion = JavaVersion.VERSION_17,
  val minGradleVersion: GradleVersion = GradleVersion.GRADLE_MIN,
  val firstUnsupportedJdkVersion: JavaVersion? = null,
  val firstUnsupportedGradleVersion: GradleVersion? = null,
) : ComparableVersion {
  // minimum supported
  AGP_MIN(Versions.ANDROID_GRADLE_MIN),

  // latest versions of each type
  AGP_STABLE(Versions.ANDROID_GRADLE_STABLE),
  AGP_RC(Versions.ANDROID_GRADLE_RC),
  AGP_BETA(Versions.ANDROID_GRADLE_BETA),
  AGP_ALPHA(Versions.ANDROID_GRADLE_ALPHA, minGradleVersion = GradleVersion.GRADLE_STABLE),
  ;

  fun isAtLeast900(): Boolean = semVer > SemVer.parse("9.0.0-beta01")
}

enum class KotlinVersion(
  override val value: String,
  val minJdkVersion: JavaVersion = JavaVersion.VERSION_17,
  val minGradleVersion: GradleVersion = GradleVersion.GRADLE_MIN,
  val firstUnsupportedJdkVersion: JavaVersion? = null,
  val firstUnsupportedGradleVersion: GradleVersion? = null,
) : ComparableVersion {
  // minimum supported
  KOTLIN_MIN(Versions.KOTLIN_MIN),

  // latest versions of each type
  KOTLIN_STABLE(Versions.KOTLIN_STABLE),
  KOTLIN_RC(Versions.KOTLIN_RC),
  KOTLIN_BETA(Versions.KOTLIN_BETA),
  KOTLIN_ALPHA(Versions.KOTLIN_ALPHA),
  ;

  fun isBelow2210(): Boolean = semVer < SemVer.parse("2.2.10")
}

enum class GradleVersion(
  override val value: String,
  val minJdkVersion: JavaVersion = JavaVersion.VERSION_17,
  val firstUnsupportedJdkVersion: JavaVersion? = null,
) : ComparableVersion {
  // minimum supported
  GRADLE_MIN(
    value = Versions.GRADLE_MIN,
    firstUnsupportedJdkVersion = JavaVersion.VERSION_25,
  ),

  // latest versions of each type
  GRADLE_STABLE(Versions.GRADLE_STABLE),
  GRADLE_RC(Versions.GRADLE_RC),
  GRADLE_BETA(Versions.GRADLE_BETA),
  GRADLE_ALPHA(Versions.GRADLE_ALPHA),
}

enum class GradlePluginPublish(
  override val value: String,
) : ComparableVersion {
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
