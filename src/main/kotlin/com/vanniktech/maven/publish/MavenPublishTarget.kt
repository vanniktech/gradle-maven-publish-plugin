package com.vanniktech.maven.publish

data class MavenPublishTarget(
  var releaseRepositoryUrl: String,
  var snapshotRepositoryUrl: String? = null,
  var repositoryUsername: String? = null,
  var repositoryPassword: String? = null,
  var signing: Boolean = true
)
