package com.vanniktech.maven.publish

import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import com.vanniktech.maven.publish.ProjectResultSubject.Companion.assertThat
import com.vanniktech.maven.publish.TestOptions.Signing.GPG_KEY
import com.vanniktech.maven.publish.TestOptions.Signing.NO_SIGNING

class SpecialCasePluginTest : BasePluginTest() {
  override val testOptions get() = TestOptions(config, NO_SIGNING, gradleVersion)

  @TestParameterInjectorTest
  fun artifactIdThatContainsProjectNameProducesCorrectArtifactId(
    @TestParameter(valuesProvider = KgpVersionProvider::class) kgpVersion: KgpVersion,
  ) {
    kgpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = kotlinMultiplatformProjectSpec(kgpVersion).copy(
      defaultProjectName = "foo",
      artifactId = "foo-bar",
    )
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).pom().exists()
    assertThat(result).pom().matchesExpectedPom(
      kotlinStdlibCommon(kgpVersion).copy(scope = "runtime"),
    )
    assertThat(result).module().exists()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().containsSourceSetFiles("commonMain")
    assertThat(result).javadocJar().exists()

    val jvmResult = result.withArtifactIdSuffix("jvm")
    assertThat(jvmResult).outcome().succeeded()
    assertThat(jvmResult).artifact("jar").exists()
    assertThat(jvmResult).pom().exists()
    assertThat(jvmResult).pom().matchesExpectedPom(
      kotlinStdlibCommon(kgpVersion),
      kotlinStdlibCommon(kgpVersion),
    )
    assertThat(jvmResult).module().exists()
    assertThat(jvmResult).sourcesJar().exists()
    assertThat(jvmResult).sourcesJar().containsSourceSetFiles("commonMain", "jvmMain")
    assertThat(jvmResult).javadocJar().exists()

    val linuxResult = result.withArtifactIdSuffix("linuxx64")
    assertThat(linuxResult).outcome().succeeded()
    assertThat(linuxResult).artifact("klib").exists()
    assertThat(linuxResult).pom().exists()
    assertThat(linuxResult).pom().matchesExpectedPom(
      "klib",
      kotlinStdlibCommon(kgpVersion),
    )
    assertThat(linuxResult).module().exists()
    assertThat(linuxResult).sourcesJar().exists()
    assertThat(linuxResult).sourcesJar().containsSourceSetFiles("commonMain", "linuxX64Main")
    assertThat(linuxResult).javadocJar().exists()

    val nodejsResult = result.withArtifactIdSuffix("nodejs")
    assertThat(nodejsResult).outcome().succeeded()
    assertThat(nodejsResult).artifact("klib").exists()
    assertThat(nodejsResult).pom().exists()
    assertThat(nodejsResult).pom().matchesExpectedPom("klib", kotlinStdlibJs(kgpVersion), kotlinDomApi(kgpVersion))
    assertThat(nodejsResult).module().exists()
    assertThat(nodejsResult).sourcesJar().exists()
    assertThat(nodejsResult).sourcesJar().containsSourceSetFiles("commonMain", "nodeJsMain")
    assertThat(nodejsResult).javadocJar().exists()
  }

  @TestParameterInjectorTest
  fun artifactIdThatContainsProjectNameProducesCorrectArtifactId2(
    @TestParameter(valuesProvider = KgpVersionProvider::class) kgpVersion: KgpVersion,
  ) {
    kgpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = kotlinMultiplatformProjectSpec(kgpVersion).copy(
      defaultProjectName = "foo",
      artifactId = "bar-foo",
    )
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).pom().exists()
    assertThat(result).pom().matchesExpectedPom(
      kotlinStdlibCommon(kgpVersion).copy(scope = "runtime"),
    )
    assertThat(result).module().exists()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().containsSourceSetFiles("commonMain")
    assertThat(result).javadocJar().exists()

    val jvmResult = result.withArtifactIdSuffix("jvm")
    assertThat(jvmResult).outcome().succeeded()
    assertThat(jvmResult).artifact("jar").exists()
    assertThat(jvmResult).pom().exists()
    assertThat(jvmResult).pom().matchesExpectedPom(
      kotlinStdlibCommon(kgpVersion),
      kotlinStdlibCommon(kgpVersion),
    )
    assertThat(jvmResult).module().exists()
    assertThat(jvmResult).sourcesJar().exists()
    assertThat(jvmResult).sourcesJar().containsSourceSetFiles("commonMain", "jvmMain")
    assertThat(jvmResult).javadocJar().exists()

    val linuxResult = result.withArtifactIdSuffix("linuxx64")
    assertThat(linuxResult).outcome().succeeded()
    assertThat(linuxResult).artifact("klib").exists()
    assertThat(linuxResult).pom().exists()
    assertThat(linuxResult).pom().matchesExpectedPom(
      "klib",
      kotlinStdlibCommon(kgpVersion),
    )
    assertThat(linuxResult).module().exists()
    assertThat(linuxResult).sourcesJar().exists()
    assertThat(linuxResult).sourcesJar().containsSourceSetFiles("commonMain", "linuxX64Main")
    assertThat(linuxResult).javadocJar().exists()

    val nodejsResult = result.withArtifactIdSuffix("nodejs")
    assertThat(nodejsResult).outcome().succeeded()
    assertThat(nodejsResult).artifact("klib").exists()
    assertThat(nodejsResult).pom().exists()
    assertThat(nodejsResult).pom().matchesExpectedPom("klib", kotlinStdlibJs(kgpVersion), kotlinDomApi(kgpVersion))
    assertThat(nodejsResult).module().exists()
    assertThat(nodejsResult).sourcesJar().exists()
    assertThat(nodejsResult).sourcesJar().containsSourceSetFiles("commonMain", "nodeJsMain")
    assertThat(nodejsResult).javadocJar().exists()
  }

  @TestParameterInjectorTest
  fun minimalPomProject() {
    val project = javaProjectSpec().copy(
      properties = emptyMap(),
    )
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).pom().exists()
    assertThat(result).pom().matchesExpectedPom(modelFactory = ::createMinimalPom)
    assertThat(result).module().exists()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
  }

  @TestParameterInjectorTest
  fun groupAndVersionFromProjectProject() {
    val project = javaProjectSpec().copy(
      group = null,
      artifactId = null,
      version = null,
      buildFileExtra =
        """
        group = "com.example.test2"
        version = "3.2.1"
        """.trimIndent(),
    )
    val result = project.run(fixtures, testProjectDir, testOptions)

    val resultSpec = project.copy(
      group = "com.example.test2",
      // the project name is used as default value for the artifact id
      artifactId = "module",
      version = "3.2.1",
    )
    val actualResult = result.copy(projectSpec = resultSpec)
    assertThat(actualResult).outcome().succeeded()
    assertThat(actualResult).artifact("jar").exists()
    assertThat(actualResult).pom().exists()
    assertThat(actualResult).pom().matchesExpectedPom()
    assertThat(actualResult).module().exists()
    assertThat(actualResult).sourcesJar().exists()
    assertThat(actualResult).sourcesJar().containsAllSourceFiles()
    assertThat(actualResult).javadocJar().exists()
  }

  @TestParameterInjectorTest
  fun withoutSigning() {
    val project = javaProjectSpec()
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isNotSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isNotSigned()
    assertThat(result).module().exists()
    assertThat(result).module().isNotSigned()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isNotSigned()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isNotSigned()
  }

  @TestParameterInjectorTest
  fun signWithGpgKey() {
    val project = javaProjectSpec()
    val result = project.run(fixtures, testProjectDir, testOptions.copy(signing = GPG_KEY))

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom()
    assertThat(result).module().exists()
    assertThat(result).module().isSigned()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSigned()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSigned()
  }
}
