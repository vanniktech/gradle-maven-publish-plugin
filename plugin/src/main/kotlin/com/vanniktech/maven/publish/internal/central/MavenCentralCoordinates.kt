package com.vanniktech.maven.publish.internal.central

internal data class MavenCentralCoordinates(
  val group: String,
  val artifactId: String,
  val version: String,
)
