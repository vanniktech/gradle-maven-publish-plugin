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
import java.util.zip.ZipFile
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.readText
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.model.io.xpp3.MavenXpp3Writer

class ProjectResultSubject private constructor(
  failureMetadata: FailureMetadata,
  private val result: ProjectResult
) : Subject(failureMetadata, result) {

  companion object {
    private val BUILD_RESULT_SUBJECT_FACTORY: Factory<ProjectResultSubject, ProjectResult> =
      Factory { metadata, actual -> ProjectResultSubject(metadata, actual) }

    fun projectResult() = BUILD_RESULT_SUBJECT_FACTORY

    fun assertThat(actual: ProjectResult): ProjectResultSubject {
      return assertAbout(projectResult()).that(actual)
    }
  }

  fun outcome(): BuildTaskSubject {
    return check("outcome").about(buildResults())
      .that(result.result)
      .task(result.task)
  }

  fun artifact(extension: String): ArtifactSubject {
    return check("artifact").about(artifact())
      .that(artifactPath("", extension) to result)
  }

  fun artifact(qualifier: String, extension: String): ArtifactSubject {
    return check("artifact").about(artifact())
      .that(artifactPath("-$qualifier", extension) to result)
  }

  fun sourcesJar(): SourcesJarSubject {
    return check("sourcesJar").about(sourcesJarSubject())
      .that(artifactPath("-sources", "jar") to result)
  }

  fun sourcesJar(qualifier: String): SourcesJarSubject {
    return check("sourcesJar").about(sourcesJarSubject())
      .that(artifactPath("-$qualifier-sources", "jar") to result)
  }

  fun javadocJar(): ArtifactSubject {
    return check("javadocJar").about(artifact())
      .that(artifactPath("-javadoc", "jar") to result)
  }

  fun javadocJar(qualifier: String): ArtifactSubject {
    return check("javadocJar").about(artifact())
      .that(artifactPath("-$qualifier-javadoc", "jar") to result)
  }

  fun pom(): PomSubject {
    return check("pom").about(pomSubject())
      .that(artifactPath("", "pom") to result)
  }

  fun module(): ArtifactSubject {
    return check("module").about(artifact())
      .that(artifactPath("", "module") to result)
  }

  private fun artifactPath(
    suffix: String,
    extension: String,
  ): Path = with(result.projectSpec) {
    return result.repo
      .resolve(group.replace(".", "/"))
      .resolve(artifactId)
      .resolve(version)
      .resolve("$artifactId-$version$suffix.$extension")
  }
}

open class ArtifactSubject internal constructor(
  failureMetadata: FailureMetadata,
  private val artifact: Path,
) : Subject(failureMetadata, artifact) {

  companion object {
    private val BUILD_RESULT_SUBJECT_FACTORY: Factory<ArtifactSubject, Pair<Path, ProjectResult>> =
      Factory { metadata, actual -> ArtifactSubject(metadata, actual.first) }

    fun artifact() = BUILD_RESULT_SUBJECT_FACTORY
  }

  fun exists() {
    if (!artifact.exists()) {
      failWithoutActual(fact("expected to exist", artifact))
    }
  }

  fun isSigned() {
    val signedArtifact = artifact.resolveSibling("${artifact.name}.asc")
    if (!signedArtifact.exists()) {
      failWithoutActual(fact("expected to exist", signedArtifact))
    }
  }
}

class SourcesJarSubject private constructor(
  failureMetadata: FailureMetadata,
  private val artifact: Path,
  private val result: ProjectResult,
) : ArtifactSubject(failureMetadata, artifact) {

  companion object {
    private val BUILD_RESULT_SUBJECT_FACTORY: Factory<SourcesJarSubject, Pair<Path, ProjectResult>> =
      Factory { metadata, actual -> SourcesJarSubject(metadata, actual.first, actual.second) }

    fun sourcesJarSubject() = BUILD_RESULT_SUBJECT_FACTORY
  }

  fun containsAllSourceFiles() {
    val zip = ZipFile(artifact.toFile())
    val zipFiles = zip.entries()
      .toList()
      .filter { !it.isDirectory && !it.name.contains("META-INF") && !it.name.contains("BuildConfig.java") }
      .toMutableList()

    val missingFiles = mutableListOf<String>()
    val notMatchingFiles = mutableListOf<Fact>()

    result.projectSpec.sourceFiles.forEach { (sourceRoot, file) ->
      // fallback is a workaround for KotlinJs creating a main folder inside the jar
      val entry = zipFiles.find { it.name == file } ?: zipFiles.find { it.name == "main/$file" }
      if (entry == null) {
        missingFiles.add(file)
      } else {
        zipFiles.remove(entry)

        val content = zip.getInputStream(entry)?.reader()?.buffered()?.readText()
        val expectedContent = result.project.resolve(sourceRoot).resolve(file).readText()
        if (content != expectedContent) {
          notMatchingFiles += fact("expected $file to equal", expectedContent)
          notMatchingFiles += fact("but was", content)
        }
      }
    }

    val facts = mutableListOf<Fact>()
    if (missingFiles.isNotEmpty() && zipFiles.isNotEmpty()) {
      facts += fact("expected to contain", missingFiles)
      facts += simpleFact("but did not.")
    }
    if (zipFiles.isNotEmpty()) {
      facts += fact("expected not to contain", zipFiles.map { it.name })
      facts += simpleFact("but did.")
    }
    facts += notMatchingFiles

    if (facts.isNotEmpty()) {
      failWithoutActual(facts.first(), *facts.drop(1).toTypedArray())
    }
  }
}

class PomSubject private constructor(
  failureMetadata: FailureMetadata,
  private val artifact: Path,
  private val result: ProjectResult,
) : ArtifactSubject(failureMetadata, artifact) {

  companion object {
    private val BUILD_RESULT_SUBJECT_FACTORY: Factory<PomSubject, Pair<Path, ProjectResult>> =
      Factory { metadata, actual -> PomSubject(metadata, actual.first, actual.second) }

    fun pomSubject() = BUILD_RESULT_SUBJECT_FACTORY
  }

  fun matchesExpectedPom(vararg dependencies: PomDependency) {
    matchesExpectedPom(null, *dependencies)
  }

  fun matchesExpectedPom(packaging: String?, vararg dependencies: PomDependency) {
    val pomWriter = MavenXpp3Writer()

    val actualModel = MavenXpp3Reader().read(artifact.inputStream())
    val actualWriter = StringWriter()
    pomWriter.write(actualWriter, actualModel)

    val expectedModel = createPom(result.projectSpec.group, result.projectSpec.artifactId, result.projectSpec.version, packaging, dependencies.toList())
    val expectedWriter = StringWriter()
    pomWriter.write(expectedWriter, expectedModel)

    assertThat(actualWriter.toString()).isEqualTo(expectedWriter.toString())
  }
}
