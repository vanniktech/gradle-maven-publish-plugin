package com.vanniktech.maven.publish

import com.google.common.truth.TruthJUnit.assume
import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import com.vanniktech.maven.publish.ProjectResultSubject.Companion.assertThat
import java.nio.file.Path
import org.junit.jupiter.api.io.TempDir

class MavenPublishPluginIntegrationTest2 {
  @TempDir
  lateinit var testProjectDir: Path

  @TestParameter(valuesProvider = TestOptionsConfigProvider::class)
  lateinit var config: TestOptions.Config

  @TestParameter
  lateinit var signing: TestOptions.Signing

  @TestParameter
  lateinit var gradleVersion: GradleVersion

  private val testOptions
    get() = TestOptions(config, signing, gradleVersion)

  @TestParameterInjectorTest
  fun javaProject() {
    val project = javaProjectSpec()
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSignedIfNeeded()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSignedIfNeeded()
    assertThat(result).pom().matchesExpectedPom()
    assertThat(result).module().exists()
    assertThat(result).module().isSignedIfNeeded()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSignedIfNeeded()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSignedIfNeeded()
  }

  @TestParameterInjectorTest
  fun javaLibraryProject() {
    val project = javaLibraryProjectSpec()
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSignedIfNeeded()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSignedIfNeeded()
    assertThat(result).pom().matchesExpectedPom()
    assertThat(result).module().exists()
    assertThat(result).module().isSignedIfNeeded()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSignedIfNeeded()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSignedIfNeeded()
  }

  @TestParameterInjectorTest
  fun javaGradlePluginProject() {
    val project = javaGradlePluginProjectSpec()
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSignedIfNeeded()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSignedIfNeeded()
    assertThat(result).pom().matchesExpectedPom()
    assertThat(result).module().exists()
    assertThat(result).module().isSignedIfNeeded()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSignedIfNeeded()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSignedIfNeeded()

    val pluginId = "com.example.test-plugin"
    val pluginMarkerSpec = project.copy(group = pluginId, artifactId = "$pluginId.gradle.plugin")
    val pluginMarkerResult = result.copy(projectSpec = pluginMarkerSpec)
    assertThat(pluginMarkerResult).pom().exists()
    assertThat(pluginMarkerResult).pom().isSignedIfNeeded()
    assertThat(pluginMarkerResult).pom().matchesExpectedPom(
      "pom",
      PomDependency("com.example", "test-artifact", "1.0.0", null)
    )
  }

  @TestParameterInjectorTest
  fun javaLibraryWithToolchainProject() {
    val project = javaLibraryProjectSpec().copy(
      buildFileExtra = """
        java {
            toolchain {
                languageVersion = JavaLanguageVersion.of(8)
            }
        }
      """.trimIndent()
    )
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSignedIfNeeded()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSignedIfNeeded()
    assertThat(result).pom().matchesExpectedPom()
    assertThat(result).module().exists()
    assertThat(result).module().isSignedIfNeeded()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSignedIfNeeded()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSignedIfNeeded()
  }

  @TestParameterInjectorTest
  fun kotlinJvmProject(@TestParameter kotlinVersion: KotlinVersion) {
    val project = kotlinJvmProjectSpec(kotlinVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSignedIfNeeded()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSignedIfNeeded()
    assertThat(result).pom().matchesExpectedPom(kotlinStdlibJdk(kotlinVersion))
    assertThat(result).module().exists()
    assertThat(result).module().isSignedIfNeeded()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSignedIfNeeded()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSignedIfNeeded()
  }

  @TestParameterInjectorTest
  fun kotlinJsProject(@TestParameter kotlinVersion: KotlinVersion) {
    val project = kotlinJsProjectSpec(kotlinVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("klib").exists()
    assertThat(result).artifact("klib").isSignedIfNeeded()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSignedIfNeeded()
    assertThat(result).pom().matchesExpectedPom("klib", kotlinStdlibJs(kotlinVersion))
    assertThat(result).module().exists()
    assertThat(result).module().isSignedIfNeeded()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSignedIfNeeded()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSignedIfNeeded()
  }

  @TestParameterInjectorTest
  fun kotlinMultiplatformProject(@TestParameter kotlinVersion: KotlinVersion) {
    val project = kotlinMultiplatformProjectSpec(kotlinVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSignedIfNeeded()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSignedIfNeeded()
    assertThat(result).pom().matchesExpectedPom(
      kotlinStdlibCommon(kotlinVersion).copy(scope = "runtime"),
    )
    assertThat(result).module().exists()
    assertThat(result).module().isSignedIfNeeded()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSignedIfNeeded()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSignedIfNeeded()

    val jvmResult = result.withArtifactIdSuffix("jvm")
    assertThat(jvmResult).outcome().succeeded()
    assertThat(jvmResult).artifact("jar").exists()
    assertThat(jvmResult).artifact("jar").isSignedIfNeeded()
    assertThat(jvmResult).pom().exists()
    assertThat(jvmResult).pom().isSignedIfNeeded()
    assertThat(jvmResult).pom().matchesExpectedPom(
      kotlinStdlibJdk(kotlinVersion),
      kotlinStdlibCommon(kotlinVersion),
    )
    assertThat(jvmResult).module().exists()
    assertThat(jvmResult).module().isSignedIfNeeded()
    assertThat(jvmResult).sourcesJar().exists()
    assertThat(jvmResult).sourcesJar().isSignedIfNeeded()
    assertThat(jvmResult).sourcesJar().containsSourceSetFiles("commonMain", "jvmMain")
    assertThat(jvmResult).javadocJar().exists()
    assertThat(jvmResult).javadocJar().isSignedIfNeeded()

    val linuxResult = result.withArtifactIdSuffix("linux")
    assertThat(linuxResult).outcome().succeeded()
    assertThat(linuxResult).artifact("klib").exists()
    assertThat(linuxResult).artifact("klib").isSignedIfNeeded()
    assertThat(linuxResult).pom().exists()
    assertThat(linuxResult).pom().isSignedIfNeeded()
    assertThat(linuxResult).pom().matchesExpectedPom(
      "klib",
      kotlinStdlibCommon(kotlinVersion)
    )
    assertThat(linuxResult).module().exists()
    assertThat(linuxResult).module().isSignedIfNeeded()
    assertThat(linuxResult).sourcesJar().exists()
    assertThat(linuxResult).sourcesJar().isSignedIfNeeded()
    assertThat(linuxResult).sourcesJar().containsSourceSetFiles("commonMain", "linuxMain")
    assertThat(linuxResult).javadocJar().exists()
    assertThat(linuxResult).javadocJar().isSignedIfNeeded()

    val nodejsResult = result.withArtifactIdSuffix("nodejs")
    assertThat(nodejsResult).outcome().succeeded()
    assertThat(nodejsResult).artifact("klib").exists()
    assertThat(nodejsResult).artifact("klib").isSignedIfNeeded()
    assertThat(nodejsResult).pom().exists()
    assertThat(nodejsResult).pom().isSignedIfNeeded()
    assertThat(nodejsResult).pom().matchesExpectedPom(
      "klib",
      kotlinStdlibJs(kotlinVersion),
      kotlinStdlibCommon(kotlinVersion),
    )
    assertThat(nodejsResult).module().exists()
    assertThat(nodejsResult).module().isSignedIfNeeded()
    assertThat(nodejsResult).sourcesJar().exists()
    assertThat(nodejsResult).sourcesJar().isSignedIfNeeded()
    assertThat(nodejsResult).sourcesJar().containsSourceSetFiles("commonMain", "nodeJsMain")
    assertThat(nodejsResult).javadocJar().exists()
    assertThat(nodejsResult).javadocJar().isSignedIfNeeded()
  }

  @TestParameterInjectorTest
  fun kotlinMultiplatformWithAndroidLibraryProject(
    @TestParameter agpVersion: AgpVersion,
    @TestParameter kotlinVersion: KotlinVersion,
  ) {
    agpVersion.assumeSupportedGradleVersion()

    val project = kotlinMultiplatformWithAndroidLibraryProjectSpec(agpVersion, kotlinVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSignedIfNeeded()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSignedIfNeeded()
    assertThat(result).pom().matchesExpectedPom(
      kotlinStdlibCommon(kotlinVersion).copy(scope = "runtime"),
    )
    assertThat(result).module().exists()
    assertThat(result).module().isSignedIfNeeded()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSignedIfNeeded()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSignedIfNeeded()

    val jvmResult = result.withArtifactIdSuffix("jvm")
    assertThat(jvmResult).outcome().succeeded()
    assertThat(jvmResult).artifact("jar").exists()
    assertThat(jvmResult).artifact("jar").isSignedIfNeeded()
    assertThat(jvmResult).pom().exists()
    assertThat(jvmResult).pom().isSignedIfNeeded()
    assertThat(jvmResult).pom().matchesExpectedPom(
      kotlinStdlibJdk(kotlinVersion),
      kotlinStdlibCommon(kotlinVersion),
    )
    assertThat(jvmResult).module().exists()
    assertThat(jvmResult).module().isSignedIfNeeded()
    assertThat(jvmResult).sourcesJar().exists()
    assertThat(jvmResult).sourcesJar().isSignedIfNeeded()
    assertThat(jvmResult).sourcesJar().containsSourceSetFiles("commonMain", "jvmMain")
    assertThat(jvmResult).javadocJar().exists()
    assertThat(jvmResult).javadocJar().isSignedIfNeeded()

    val linuxResult = result.withArtifactIdSuffix("linux")
    assertThat(linuxResult).outcome().succeeded()
    assertThat(linuxResult).artifact("klib").exists()
    assertThat(linuxResult).artifact("klib").isSignedIfNeeded()
    assertThat(linuxResult).pom().exists()
    assertThat(linuxResult).pom().isSignedIfNeeded()
    assertThat(linuxResult).pom().matchesExpectedPom(
      "klib",
      kotlinStdlibCommon(kotlinVersion)
    )
    assertThat(linuxResult).module().exists()
    assertThat(linuxResult).module().isSignedIfNeeded()
    assertThat(linuxResult).sourcesJar().exists()
    assertThat(linuxResult).sourcesJar().isSignedIfNeeded()
    assertThat(linuxResult).sourcesJar().containsSourceSetFiles("commonMain", "linuxMain")
    assertThat(linuxResult).javadocJar().exists()
    assertThat(linuxResult).javadocJar().isSignedIfNeeded()

    val nodejsResult = result.withArtifactIdSuffix("nodejs")
    assertThat(nodejsResult).outcome().succeeded()
    assertThat(nodejsResult).artifact("klib").exists()
    assertThat(nodejsResult).artifact("klib").isSignedIfNeeded()
    assertThat(nodejsResult).pom().exists()
    assertThat(nodejsResult).pom().isSignedIfNeeded()
    assertThat(nodejsResult).pom().matchesExpectedPom(
      "klib",
      kotlinStdlibJs(kotlinVersion),
      kotlinStdlibCommon(kotlinVersion),
    )
    assertThat(nodejsResult).module().exists()
    assertThat(nodejsResult).module().isSignedIfNeeded()
    assertThat(nodejsResult).sourcesJar().exists()
    assertThat(nodejsResult).sourcesJar().isSignedIfNeeded()
    assertThat(nodejsResult).sourcesJar().containsSourceSetFiles("commonMain", "nodeJsMain")
    assertThat(nodejsResult).javadocJar().exists()
    assertThat(nodejsResult).javadocJar().isSignedIfNeeded()

    val androidReleaseResult = result.withArtifactIdSuffix("android")
    assertThat(androidReleaseResult).outcome().succeeded()
    assertThat(androidReleaseResult).artifact("aar").exists()
    assertThat(androidReleaseResult).artifact("aar").isSignedIfNeeded()
    assertThat(androidReleaseResult).pom().exists()
    assertThat(androidReleaseResult).pom().isSignedIfNeeded()
    assertThat(androidReleaseResult).pom().matchesExpectedPom(
      "aar",
      kotlinStdlibJdk(kotlinVersion),
      kotlinStdlibCommon(kotlinVersion),
    )
    assertThat(androidReleaseResult).module().exists()
    assertThat(androidReleaseResult).module().isSignedIfNeeded()
    assertThat(androidReleaseResult).sourcesJar().exists()
    assertThat(androidReleaseResult).sourcesJar().isSignedIfNeeded()
    assertThat(androidReleaseResult).sourcesJar().containsSourceSetFiles("commonMain", "androidMain", "androidRelease")
    assertThat(androidReleaseResult).javadocJar().exists()
    assertThat(androidReleaseResult).javadocJar().isSignedIfNeeded()

    val androidDebugResult = result.withArtifactIdSuffix("android-debug")
    assertThat(androidDebugResult).outcome().succeeded()
    assertThat(androidDebugResult).artifact("aar").exists()
    assertThat(androidDebugResult).artifact("aar").isSignedIfNeeded()
    assertThat(androidDebugResult).pom().exists()
    assertThat(androidDebugResult).pom().isSignedIfNeeded()
    assertThat(androidDebugResult).pom().matchesExpectedPom(
      "aar",
      kotlinStdlibJdk(kotlinVersion),
      kotlinStdlibCommon(kotlinVersion),
    )
    assertThat(androidDebugResult).module().exists()
    assertThat(androidDebugResult).module().isSignedIfNeeded()
    assertThat(androidDebugResult).sourcesJar().exists()
    assertThat(androidDebugResult).sourcesJar().isSignedIfNeeded()
    assertThat(androidDebugResult).sourcesJar().containsSourceSetFiles("commonMain", "androidMain", "androidDebug")
    assertThat(androidDebugResult).javadocJar().exists()
    assertThat(androidDebugResult).javadocJar().isSignedIfNeeded()
  }

  @TestParameterInjectorTest
  fun androidLibraryProject(@TestParameter agpVersion: AgpVersion) {
    agpVersion.assumeSupportedGradleVersion()

    val project = androidLibraryProjectSpec(agpVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("aar").exists()
    assertThat(result).artifact("aar").isSignedIfNeeded()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSignedIfNeeded()
    assertThat(result).pom().matchesExpectedPom("aar")
    assertThat(result).module().exists()
    assertThat(result).module().isSignedIfNeeded()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSignedIfNeeded()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSignedIfNeeded()
  }

  @TestParameterInjectorTest
  fun androidLibraryKotlinProject(
    @TestParameter agpVersion: AgpVersion,
    @TestParameter kotlinVersion: KotlinVersion,
  ) {
    agpVersion.assumeSupportedGradleVersion()

    val project = androidLibraryKotlinProjectSpec(agpVersion, kotlinVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("aar").exists()
    assertThat(result).artifact("aar").isSignedIfNeeded()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSignedIfNeeded()
    assertThat(result).pom().matchesExpectedPom("aar", kotlinStdlibJdk(kotlinVersion))
    assertThat(result).module().exists()
    assertThat(result).module().isSignedIfNeeded()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSignedIfNeeded()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSignedIfNeeded()
  }

  @TestParameterInjectorTest
  fun minimalPomProject() {
    val project = javaProjectSpec().copy(
      properties = emptyMap()
    )
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSignedIfNeeded()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSignedIfNeeded()
    assertThat(result).pom().matchesExpectedPom(modelFactory = ::createMinimalPom)
    assertThat(result).module().exists()
    assertThat(result).module().isSignedIfNeeded()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSignedIfNeeded()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSignedIfNeeded()
  }

  @TestParameterInjectorTest
  fun groupAndVersionFromProjectProject() {
    val project = javaProjectSpec().copy(
      group = null,
      version = null,
      buildFileExtra = """
        group = "com.example.test2"
        version = "3.2.1"
      """.trimIndent()
    )
    val result = project.run(fixtures, testProjectDir, testOptions)

    val resultSpec = project.copy(
      group = "com.example.test2",
      version = "3.2.1",
    )
    val actualResult = result.copy(projectSpec = resultSpec)
    assertThat(actualResult).outcome().succeeded()
    assertThat(actualResult).artifact("jar").exists()
    assertThat(actualResult).artifact("jar").isSignedIfNeeded()
    assertThat(actualResult).pom().exists()
    assertThat(actualResult).pom().isSignedIfNeeded()
    assertThat(actualResult).pom().matchesExpectedPom()
    assertThat(actualResult).module().exists()
    assertThat(actualResult).module().isSignedIfNeeded()
    assertThat(actualResult).sourcesJar().exists()
    assertThat(actualResult).sourcesJar().isSignedIfNeeded()
    assertThat(actualResult).sourcesJar().containsAllSourceFiles()
    assertThat(actualResult).javadocJar().exists()
    assertThat(actualResult).javadocJar().isSignedIfNeeded()
  }

  private fun AgpVersion.assumeSupportedGradleVersion() {
    assume().that(gradleVersion).isAtLeast(minGradleVersion)
    if (firstUnsupportedGradleVersion != null) {
      assume().that(gradleVersion).isLessThan(firstUnsupportedGradleVersion)
    }
  }

  private fun ArtifactSubject.isSignedIfNeeded() {
    if (signing != TestOptions.Signing.NO_SIGNING) {
      isSigned()
    }
  }
}
