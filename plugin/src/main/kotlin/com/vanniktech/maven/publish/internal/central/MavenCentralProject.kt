package com.vanniktech.maven.publish.internal.central

import java.io.File

internal data class MavenCentralProject(
  val coordinates: MavenCentralCoordinates,
  val localRepository: File,
)
