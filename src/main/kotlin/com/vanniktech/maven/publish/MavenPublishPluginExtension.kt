package com.vanniktech.maven.publish

import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

/**
 * Extension for maven publish plugin.
 * @since 0.1.0
 */
open class MavenPublishPluginExtension(project: Project) {

  private val defaultTarget = MavenPublishTarget(
      DEFAULT_TARGET,
      project.findProperty("RELEASE_REPOSITORY_URL") as String? ?: System.getenv("RELEASE_REPOSITORY_URL") ?: "https://oss.sonatype.org/service/local/staging/deploy/maven2/",
      project.findProperty("SNAPSHOT_REPOSITORY_URL") as String? ?: System.getenv("SNAPSHOT_REPOSITORY_URL") ?: "https://oss.sonatype.org/content/repositories/snapshots/",
      project.findProperty("SONATYPE_NEXUS_USERNAME") as String? ?: System.getenv("SONATYPE_NEXUS_USERNAME"),
      project.findProperty("SONATYPE_NEXUS_PASSWORD") as String? ?: System.getenv("SONATYPE_NEXUS_PASSWORD")
  )

  private val localTarget = MavenPublishTarget(
      LOCAL_TARGET,
      releaseRepositoryUrl = project.repositories.mavenLocal().url.toASCIIString(),
      signing = false
  )

  /**
   * If set to false the new `maven-publish` plugin will be used instead of the soon to be deprecated
   * `maven` plugin.
   *
   * This is **experimental** and Android projects are unsupported until
   * [this issue][https://issuetracker.google.com/issues/37055147] is fixed.
   *
   * @Since 0.9.0
   */
  var useLegacyMode: Boolean = true

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
   * Allows to add additional [MavenPublishTargets][MavenPublishTarget] to publish to multiple repositories.
   * @since 0.7.0
   */
  val targets: NamedDomainObjectContainer<MavenPublishTarget> =
      project.container(MavenPublishTarget::class.java) { MavenPublishTarget(it) }.apply {
        add(defaultTarget)
        add(localTarget)
      }

  /**
   * Allows to add additional [MavenPublishTargets][MavenPublishTarget] to publish to multiple repositories.
   * @since 0.7.0
   */
  fun targets(closure: Closure<*>) {
    targets.configure(closure)
  }

  internal companion object {
    internal const val DEFAULT_TARGET = "uploadArchives"
    internal const val LOCAL_TARGET = "installArchives"
  }
}
