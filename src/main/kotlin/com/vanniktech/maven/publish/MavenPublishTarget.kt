package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.MavenPublishPluginExtension.Companion.DEFAULT_TARGET
import com.vanniktech.maven.publish.MavenPublishPluginExtension.Companion.LOCAL_TARGET
import org.gradle.api.artifacts.transform.InputArtifactDependencies

data class MavenPublishTarget(
  internal val name: String,
  /**
   * The release repository url this should be published to.
   * @since 0.7.0
   */
  var releaseRepositoryUrl: String? = null,

  /**
   * The snapshot repository url this should be published to.
   * @since 0.7.0
   */
  var snapshotRepositoryUrl: String? = null,

  /**
   * The username that should be used for publishing.
   * @since 0.7.0
   */
  var repositoryUsername: String? = null,

  /**
   * The password that should be used for publishing.
   * @since 0.7.0
   */
  var repositoryPassword: String? = null,

  /**
   * Whether release artifacts should be signed before uploading to this target.
   * @since 0.7.0
   */
  @Deprecated("Disabling signing on a target level is not supported anymore. See releaseSigningEnabled for a replacement")
  var signing: Boolean = true
) {

  val taskName get(): String {
    if (name == DEFAULT_TARGET || name == LOCAL_TARGET) {
      return name
    } else {
      return DEFAULT_TARGET + name.capitalize()
    }
  }
}
