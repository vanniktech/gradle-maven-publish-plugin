package com.vanniktech.maven.publish.central

internal data class MavenCentralCoordinates(
  val group: String,
  val artifactId: String,
  val version: String,
)
