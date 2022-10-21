package com.vanniktech.maven.publish

import java.nio.file.Path
import org.gradle.testkit.runner.BuildResult

data class ProjectSpec(
  val plugins: List<PluginSpec>,
  val group: String,
  val artifactId: String,
  val version: String,
  val properties: Map<String, String>,
  val sourceFiles: List<Pair<String, String>>,
  val buildFileExtra: String = ""
)

data class PluginSpec(
  val id: String,
  val version: String? = null,
)

data class ProjectResult(
  val result: BuildResult?,
  val task: String,
  val projectSpec: ProjectSpec,
  val project: Path,
  val repo: Path,
)
