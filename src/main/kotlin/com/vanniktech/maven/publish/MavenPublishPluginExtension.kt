package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.nexus.NexusOptions
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

/**
 * Extension for maven publish plugin.
 * @since 0.1.0
 */
open class MavenPublishPluginExtension(project: Project) {

  @Suppress("deprecation")
  internal val defaultTarget = MavenPublishTarget(
      DEFAULT_TARGET,
      project.findOptionalProperty("RELEASE_REPOSITORY_URL") ?: System.getenv("RELEASE_REPOSITORY_URL") ?: "https://oss.sonatype.org/service/local/staging/deploy/maven2/",
      project.findOptionalProperty("SNAPSHOT_REPOSITORY_URL") ?: System.getenv("SNAPSHOT_REPOSITORY_URL") ?: "https://oss.sonatype.org/content/repositories/snapshots/",
      project.findOptionalProperty("SONATYPE_NEXUS_USERNAME") ?: System.getenv("SONATYPE_NEXUS_USERNAME") ?: project.findOptionalProperty("mavenCentralRepositoryUsername"),
      project.findOptionalProperty("SONATYPE_NEXUS_PASSWORD") ?: System.getenv("SONATYPE_NEXUS_PASSWORD") ?: project.findOptionalProperty("mavenCentralRepositoryPassword")
  )

  @Suppress("deprecation")
  internal val localTarget = MavenPublishTarget(
      LOCAL_TARGET,
      releaseRepositoryUrl = project.repositories.mavenLocal().url.toASCIIString(),
      signing = false
  )

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

  /**
   * Allows to promote repositories without connecting to the nexus instance console.
   * @since 0.9.0
   */
  var nexusOptions = NexusOptions.fromProject(project)

  /**
   * Allows to promote repositories without connecting to the nexus instance console.
   * @since 0.9.0
   */
  fun nexus(action: Action<NexusOptions>) {
    action.execute(nexusOptions)
  }

  /**
   * Allows to add additional [MavenPublishTargets][MavenPublishTarget] to publish to multiple repositories.
   * @since 0.7.0
   * @deprecated Use Gradle publishing API instead https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories
   */
  @Deprecated("Use Gradle publishing API instead")
  @Suppress("DEPRECATION")
  val targets: NamedDomainObjectContainer<MavenPublishTarget> =
      project.container(MavenPublishTarget::class.java) { MavenPublishTarget(it) }.apply {
        add(defaultTarget)
        add(localTarget)
      }

  /**
   * Allows to add additional [MavenPublishTargets][MavenPublishTarget] to publish to multiple repositories.
   * @since 0.7.0
   * @deprecated Use Gradle publishing API instead https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories
   */
  @Deprecated("Use Gradle publishing API instead")
  @Suppress("DEPRECATION")
  fun targets(closure: Closure<*>) {
    targets.configure(closure)
  }

  internal companion object {
    internal const val DEFAULT_TARGET = "uploadArchives"
    internal const val LOCAL_TARGET = "installArchives"
  }
}
