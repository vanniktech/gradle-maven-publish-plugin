package com.vanniktech.maven.publish.central

import java.io.File

internal data class MavenCentralProject(
  val coordinates: MavenCentralCoordinates,
  val localRepository: File,
)
