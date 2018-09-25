package com.vanniktech.maven.publish

data class MavenPublishTarget(
  internal val name: String,
  /**
   * The release repository url this should be published to.
   * @since 0.7.0
   */
  var releaseRepositoryUrl: String,

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
  var signing: Boolean = true
)
