package com.vanniktech.maven.publish

import com.google.common.truth.TruthJUnit.assume
import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import com.vanniktech.maven.publish.ProjectResultSubject.Companion.assertThat
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.jupiter.api.io.TempDir

class MavenPublishPluginIntegrationTest2 {
  @TempDir
  lateinit var testProjectDir: Path

  private val config: TestOptions.Config = TestOptions.Config.valueOf(System.getProperty("testConfigMethod"))

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

  companion object {
    private val fixtures = Paths.get("src/integrationTest/fixtures2").toAbsolutePath()
  }
}
