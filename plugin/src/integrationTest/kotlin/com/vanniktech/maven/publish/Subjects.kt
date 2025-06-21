package com.vanniktech.maven.publish

import com.autonomousapps.kit.truth.BuildResultSubject.Companion.buildResults
import com.autonomousapps.kit.truth.BuildTaskSubject
import com.google.common.truth.Fact
import com.google.common.truth.Fact.fact
import com.google.common.truth.Fact.simpleFact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.vanniktech.maven.publish.ArtifactSubject.Companion.artifact
import com.vanniktech.maven.publish.PomSubject.Companion.pomSubject
import com.vanniktech.maven.publish.SourcesJarSubject.Companion.sourcesJarSubject
import java.io.StringWriter
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.readText
import org.apache.maven.model.Dependency
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.model.io.xpp3.MavenXpp3Writer

class ProjectResultSubject private constructor(
  failureMetadata: FailureMetadata,
  private val result: ProjectResult,
) : Subject(failureMetadata, result) {
  companion object {
    private val BUILD_RESULT_SUBJECT_FACTORY: Factory<ProjectResultSubject, ProjectResult> =
      Factory { metadata, actual -> ProjectResultSubject(metadata, actual!!) }

    fun projectResult() = BUILD_RESULT_SUBJECT_FACTORY

    fun assertThat(actual: ProjectResult): ProjectResultSubject = assertAbout(projectResult()).that(actual)
  }

  fun outcome(): BuildTaskSubject = check("outcome")
    .about(buildResults())
    .that(result.result)
    .task(result.task)

  fun artifact(extension: String): ArtifactSubject = check("artifact")
    .about(artifact())
    .that(artifactPath("", extension) to result)

  fun artifact(qualifier: String, extension: String): ArtifactSubject = check("artifact")
    .about(artifact())
    .that(artifactPath("-$qualifier", extension) to result)

  fun sourcesJar(): SourcesJarSubject = check("sourcesJar")
    .about(sourcesJarSubject())
    .that(artifactPath("-sources", "jar") to result)

  fun sourcesJar(qualifier: String): SourcesJarSubject = check("sourcesJar")
    .about(sourcesJarSubject())
    .that(artifactPath("-$qualifier-sources", "jar") to result)

  fun javadocJar(): ArtifactSubject = check("javadocJar")
    .about(artifact())
    .that(artifactPath("-javadoc", "jar") to result)

  fun javadocJar(qualifier: String): ArtifactSubject = check("javadocJar")
    .about(artifact())
    .that(artifactPath("-$qualifier-javadoc", "jar") to result)

  fun pom(): PomSubject = check("pom")
    .about(pomSubject())
    .that(artifactPath("", "pom") to result)

  fun module(): ArtifactSubject = check("module")
    .about(artifact())
    .that(artifactPath("", "module") to result)

  private fun artifactPath(suffix: String, extension: String): Path = with(result.projectSpec) {
    return result.repo
      .resolve(group!!.replace(".", "/"))
      .resolve(artifactId!!)
      .resolve(version!!)
      .resolve("$artifactId-$version$suffix.$extension")
  }
}

open class ArtifactSubject internal constructor(
  failureMetadata: FailureMetadata,
  private val artifact: Path,
  private val result: ProjectResult,
) : Subject(failureMetadata, artifact) {
  companion object {
    private val BUILD_RESULT_SUBJECT_FACTORY: Factory<ArtifactSubject, Pair<Path, ProjectResult>> =
      Factory { metadata, actual -> ArtifactSubject(metadata, actual!!.first, actual.second) }

    fun artifact() = BUILD_RESULT_SUBJECT_FACTORY
  }

  fun exists() {
    if (!artifact.exists()) {
      val files = result.repo
        .toFile()
        .walkTopDown()
        .filter { it.isFile }
        .toList()
      failWithActual(fact("expected to exist", artifact), fact("but repo contained", files))
    }
  }

  fun doesNotExist() {
    if (artifact.exists()) {
      val files = result.repo
        .toFile()
        .walkTopDown()
        .filter { it.isFile }
        .toList()
      failWithActual(fact("expected not to exist", artifact), fact("but repo contained", files))
    }
  }

  fun isSigned() {
    val signedArtifact = artifact.resolveSibling("${artifact.name}.asc")
    if (!signedArtifact.exists()) {
      failWithoutActual(fact("expected to exist", signedArtifact))
    }
  }

  fun isNotSigned() {
    val signedArtifact = artifact.resolveSibling("${artifact.name}.asc")
    if (signedArtifact.exists()) {
      failWithoutActual(fact("expected not to exist", signedArtifact))
    }
  }

  fun containsFiles(ignoreAdditionalFiles: Boolean, vararg files: String) {
    containsMatchingFiles(
      filesToFind = files.toList(),
      filesToIgnore = emptyList(),
      failWhenAdditionalFilesFound = !ignoreAdditionalFiles,
      fileMatcher = { sourceFile, zipEntry -> zipEntry.name == sourceFile },
      fileDescriptor = { it },
      fileContent = { null },
    )
  }

  protected fun <T : Any> containsMatchingFiles(
    filesToFind: List<T>,
    filesToIgnore: List<String>,
    failWhenAdditionalFilesFound: Boolean,
    fileMatcher: (T, ZipEntry) -> Boolean,
    fileDescriptor: (T) -> String,
    // only match file content if this does not return null
    fileContent: (T) -> String?,
  ) {
    val zip = ZipFile(artifact.toFile())
    val zipFiles = zip
      .entries()
      .toList()
      .filter { zipEntry -> !zipEntry.isDirectory && filesToIgnore.none { zipEntry.name.contains(it) } }
      .toMutableList()

    val missingFiles = mutableListOf<String>()
    val notMatchingFiles = mutableListOf<Fact>()

    filesToFind.forEach { sourceFile ->
      // fallback is a workaround for Kotlin creating a main folder inside the jar
      val entry = zipFiles.find { fileMatcher(sourceFile, it) }
      if (entry == null) {
        missingFiles.add(fileDescriptor(sourceFile))
      } else {
        zipFiles.remove(entry)

        val content = zip
          .getInputStream(entry)
          ?.reader()
          ?.buffered()
          ?.readText()
        val expectedContent = fileContent(sourceFile)
        if (expectedContent != null && expectedContent != content) {
          notMatchingFiles += fact("expected ${fileDescriptor(sourceFile)} to equal", expectedContent)
          notMatchingFiles += fact("but was", content)
        }
      }
    }

    val facts = mutableListOf<Fact>()

    if (missingFiles.isNotEmpty()) {
      facts += fact("expected to contain", missingFiles)
      facts += simpleFact("but did not.")
    }

    if (failWhenAdditionalFilesFound) {
      if (zipFiles.isNotEmpty()) {
        facts += fact("expected not to contain", zipFiles.map { it.name })
        facts += simpleFact("but did.")
      }
    }

    facts += notMatchingFiles

    if (facts.isNotEmpty()) {
      failWithoutActual(facts.first(), *facts.drop(1).toTypedArray())
    }
  }
}

class SourcesJarSubject private constructor(
  failureMetadata: FailureMetadata,
  artifact: Path,
  private val result: ProjectResult,
) : ArtifactSubject(failureMetadata, artifact, result) {
  companion object {
    private val BUILD_RESULT_SUBJECT_FACTORY: Factory<SourcesJarSubject, Pair<Path, ProjectResult>> =
      Factory { metadata, actual -> SourcesJarSubject(metadata, actual!!.first, actual.second) }

    fun sourcesJarSubject() = BUILD_RESULT_SUBJECT_FACTORY
  }

  fun containsAllSourceFiles() {
    containsSourceFiles(result.projectSpec.sourceFiles)
  }

  fun containsSourceSetFiles(vararg sourceSets: String) {
    containsSourceFiles(result.projectSpec.sourceFiles.filter { sourceSets.contains(it.sourceSet) })
  }

  private fun containsSourceFiles(sourceFiles: List<SourceFile>) {
    containsMatchingFiles(
      filesToFind = sourceFiles,
      filesToIgnore = listOf("META-INF", "BuildConfig.java"),
      failWhenAdditionalFilesFound = true,
      fileMatcher = { sourceFile, zipEntry ->
        zipEntry.name == sourceFile.file || zipEntry.name == "${sourceFile.sourceSet}/${sourceFile.file}"
      },
      fileDescriptor = { "${it.sourceSet}/${it.file}" },
      fileContent = { it.resolveIn(result.project).readText() },
    )
  }
}

class PomSubject private constructor(
  failureMetadata: FailureMetadata,
  private val artifact: Path,
  private val result: ProjectResult,
) : ArtifactSubject(failureMetadata, artifact, result) {
  companion object {
    private val BUILD_RESULT_SUBJECT_FACTORY: Factory<PomSubject, Pair<Path, ProjectResult>> =
      Factory { metadata, actual -> PomSubject(metadata, actual!!.first, actual.second) }

    fun pomSubject() = BUILD_RESULT_SUBJECT_FACTORY
  }

  fun matchesExpectedPom(vararg dependencies: PomDependency) {
    matchesExpectedPom(dependencies = dependencies.toList())
  }

  fun matchesExpectedPom(packaging: String, vararg dependencies: PomDependency) {
    matchesExpectedPom(packaging, dependencies.toList())
  }

  fun matchesExpectedPom(
    packaging: String? = null,
    dependencies: List<PomDependency> = emptyList(),
    dependencyManagementDependencies: List<PomDependency> = emptyList(),
    modelFactory: (String, String, String, String?, List<PomDependency>, List<PomDependency>) -> Model = ::createPom,
  ) {
    val pomWriter = MavenXpp3Writer()

    val actualModel = MavenXpp3Reader().read(artifact.inputStream())
    actualModel.sortDependencies()
    val actualWriter = StringWriter()
    pomWriter.write(actualWriter, actualModel)

    val expectedModel = modelFactory(
      result.projectSpec.group!!,
      result.projectSpec.artifactId!!,
      result.projectSpec.version!!,
      packaging,
      dependencies,
      dependencyManagementDependencies,
    )
    expectedModel.sortDependencies()
    val expectedWriter = StringWriter()
    pomWriter.write(expectedWriter, expectedModel)

    assertThat(actualWriter.toString()).isEqualTo(expectedWriter.toString())
  }

  private fun Model.sortDependencies() {
    dependencies = dependencies.sortedWith(comparator)
    if (dependencyManagement != null) {
      dependencyManagement.dependencies = dependencyManagement.dependencies.sortedWith(comparator)
    }
  }

  private val comparator = compareBy<Dependency> {
    "${it.scope} ${it.groupId}:${it.artifactId}:${it.version}@${it.classifier}"
  }
}
