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
    project.findProperty("SONATYPE_NEXUS_PASSWORD") as String? ?: System.getenv("SONATYPE_NEXUS_PASSWORD"))

  private val localTarget = MavenPublishTarget(
    LOCAL_TARGET,
    releaseRepositoryUrl = project.repositories.mavenLocal().url.toASCIIString(),
    signing = false)

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
    internal const val DEFAULT_TARGET = "upload"
    internal const val LOCAL_TARGET = "install"
  }
}
