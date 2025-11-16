package com.vanniktech.maven.publish.util

import java.nio.file.Path
import org.gradle.testkit.runner.BuildResult

data class ProjectSpec(
  val plugins: List<PluginSpec>,
  val group: String,
  val artifactId: String,
  val version: String,
  val properties: Map<String, String>,
  val sourceFiles: List<SourceFile>,
  val basePluginConfig: String,
  val defaultProjectName: String = "default-root-project-name",
  val buildFileExtra: String = "",
  val propertiesExtra: String = "",
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
) {
  fun withArtifactIdSuffix(suffix: String): ProjectResult {
    val updatedSpec = projectSpec.copy(artifactId = "${projectSpec.artifactId}-$suffix")
    return copy(projectSpec = updatedSpec)
  }
}

data class SourceFile(
  val sourceSet: String,
  val sourceSetFolder: String,
  val file: String,
) {
  fun resolveIn(root: Path): Path = root.resolve("src/$sourceSet/$sourceSetFolder/$file")
}
