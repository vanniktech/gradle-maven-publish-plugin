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
}
