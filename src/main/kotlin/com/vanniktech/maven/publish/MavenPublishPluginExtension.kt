package com.vanniktech.maven.publish

import org.gradle.api.Project

/**
 * Extension for maven publish plugin.
 * @since 0.1.0
 */
@Suppress("UnnecessaryAbstractClass")
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
  var sonatypeHost: SonatypeHost? = SonatypeHost.DEFAULT

  /**
   * The Android library variant that should be published. Projects not using any product flavors, that just want
   * to publish the release build type can use the default.
   *
   * This is **not supported** in legacy mode.
   *
   * @Since 0.9.0
   */
  var androidVariantToPublish: String = "release"

  /**
   * Whether release artifacts should be signed before getting published.
   *
   * @Since 0.9.0
   */
  var releaseSigningEnabled: Boolean = project.findOptionalProperty("RELEASE_SIGNING_ENABLED")?.toBoolean() ?: true
}
