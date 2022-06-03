package com.vanniktech.maven.publish

import org.gradle.api.Project

/**
 * Extension for maven publish plugin.
 * @since 0.1.0
 */
abstract class MavenPublishPluginExtension(
  private val project: Project
) {

  /**
   * Sonatype splits users between 2 different instances. If you are using `oss.sonatype.org`, this should be set to
   * [SonatypeHost.DEFAULT]. For `s01.oss.sonatype.org` [SonatypeHost.SO1] should be used instead.
   *
   * For more information see: https://central.sonatype.org/articles/2021/Feb/23/new-users-on-s01osssonatypeorg/
   *
   * @Since 0.15.0
   */
  @Deprecated("Set the SONATYPE_HOST Gradle property or call mavenPublishing { publishToMavenCentral(\"<VALUE>\") } instead")
  var sonatypeHost: SonatypeHost? = defaultSonatypeHost()

  /**
   * Whether release artifacts should be signed before getting published.
   *
   * @Since 0.9.0
   */
  @Deprecated("Set the RELEASE_SIGNING_ENABLED Gradle property or call mavenPublishing { signAllPublications() } instead")
  var releaseSigningEnabled: Boolean = releaseSigningProperty() ?: true

  internal fun sonatypeHostProperty(): String? {
    return project.findOptionalProperty("SONATYPE_HOST")
  }

  internal fun releaseSigningProperty(): Boolean? {
    return project.findOptionalProperty("RELEASE_SIGNING_ENABLED")?.toBoolean()
  }

  private fun defaultSonatypeHost(): SonatypeHost? {
    val property = sonatypeHostProperty()
    if (property != null) {
      return if (property.isBlank()) {
        null
      } else {
        SonatypeHost.valueOf(property)
      }
    }

    return SonatypeHost.DEFAULT
  }
}
