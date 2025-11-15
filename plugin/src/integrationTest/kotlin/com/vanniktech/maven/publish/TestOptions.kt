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

sealed class ComparableVersion(
  private val name: String,
) : Comparable<ComparableVersion> {
  abstract val value: String
  val semVer: SemVer get() = SemVer.parse(value)

  override fun compareTo(other: ComparableVersion): Int = semVer.compareTo(other.semVer)

  override fun equals(other: Any?): Boolean = semVer == (other as? ComparableVersion)?.semVer

  override fun hashCode(): Int = semVer.hashCode()

  override fun toString(): String = "$name($semVer)"
}

class AgpVersion(
  override val value: String,
  val minJdkVersion: JavaVersion = JavaVersion.VERSION_17,
  val minGradleVersion: GradleVersion = GradleVersion.VERSIONS.min(),
  val firstUnsupportedJdkVersion: JavaVersion? = null,
  val firstUnsupportedGradleVersion: GradleVersion? = null,
) : ComparableVersion("AGP") {
  companion object {
    val VERSIONS = setOf(
      // minimum supported
      AgpVersion(Versions.ANDROID_GRADLE_MIN),
      // latest versions of each type
      AgpVersion(Versions.ANDROID_GRADLE_STABLE),
      AgpVersion(Versions.ANDROID_GRADLE_RC),
      AgpVersion(Versions.ANDROID_GRADLE_BETA, minGradleVersion = GradleVersion.GRADLE_9_1_0),
      AgpVersion(Versions.ANDROID_GRADLE_ALPHA, minGradleVersion = GradleVersion.GRADLE_9_1_0),
    )

    // versions used for checks instead of test matrix
    val AGP_9_0_0 = AgpVersion("9.0.0-alpha01")
  }
}

class KgpVersion(
  override val value: String,
  val minJdkVersion: JavaVersion = JavaVersion.VERSION_17,
  val minGradleVersion: GradleVersion = GradleVersion.VERSIONS.min(),
  val firstUnsupportedJdkVersion: JavaVersion? = null,
  val firstUnsupportedGradleVersion: GradleVersion? = null,
) : ComparableVersion("KGP") {
  companion object {
    val VERSIONS = setOf(
      // minimum supported
      KgpVersion(Versions.KOTLIN_MIN),
      // latest versions of each type
      KgpVersion(Versions.KOTLIN_STABLE),
      KgpVersion(Versions.KOTLIN_RC),
      KgpVersion(Versions.KOTLIN_BETA),
      KgpVersion(Versions.KOTLIN_ALPHA),
    )

    // versions used for checks instead of test matrix
    val KOTLIN_2_2_10 = KgpVersion("2.2.10")
  }
}

class GradleVersion(
  override val value: String,
  val minJdkVersion: JavaVersion = JavaVersion.VERSION_17,
  val firstUnsupportedJdkVersion: JavaVersion? = null,
) : ComparableVersion("Gradle") {
  companion object {
    val VERSIONS = setOf(
      // minimum supported
      GradleVersion(
        value = Versions.GRADLE_MIN,
        firstUnsupportedJdkVersion = JavaVersion.VERSION_25,
      ),
      // latest versions of each type
      GradleVersion(Versions.GRADLE_STABLE),
      GradleVersion(Versions.GRADLE_RC),
      GradleVersion(Versions.GRADLE_BETA),
      GradleVersion(Versions.GRADLE_ALPHA),
    )

    // versions used for checks instead of test matrix
    val GRADLE_9_1_0 = GradleVersion("9.1.0")
  }
}

class PluginPublishVersion(
  override val value: String,
) : ComparableVersion("PluginPublish") {
  companion object {
    val VERSIONS = setOf(
      // minimum supported
      PluginPublishVersion("1.0.0"),
      // latest versions of each type
      PluginPublishVersion(Versions.GRADLE_PUBLISH_STABLE),
      PluginPublishVersion(Versions.GRADLE_PUBLISH_RC),
      PluginPublishVersion(Versions.GRADLE_PUBLISH_BETA),
      PluginPublishVersion(Versions.GRADLE_PUBLISH_ALPHA),
    )
  }
}

fun GradleVersion.assumeSupportedJdkVersion() {
  assume().that(JavaVersion.current()).isAtLeast(minJdkVersion)
  if (firstUnsupportedJdkVersion != null) {
    assume().that(JavaVersion.current()).isLessThan(firstUnsupportedJdkVersion)
  }
}

fun KgpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion: GradleVersion) {
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
