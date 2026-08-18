package com.vanniktech.maven.publish

import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import com.vanniktech.maven.publish.util.KgpVersion
import com.vanniktech.maven.publish.util.KgpVersionProvider
import com.vanniktech.maven.publish.util.ProjectResultSubject.Companion.assertThat
import com.vanniktech.maven.publish.util.TestOptions
import com.vanniktech.maven.publish.util.TestOptions.Signing.GPG_KEY
import com.vanniktech.maven.publish.util.TestOptions.Signing.NO_SIGNING
import com.vanniktech.maven.publish.util.assumeSupportedJdkAndGradleVersion
import com.vanniktech.maven.publish.util.createMinimalPom
import com.vanniktech.maven.publish.util.javaProjectSpec
import com.vanniktech.maven.publish.util.kotlinMultiplatformProjectSpec

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
    val result = project.run()

    assertThat(result).hasKotlinArtifactsCommon(kgpVersion, containsAndroidTarget = false, enableSigning = false)
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
    val result = project.run()

    assertThat(result).hasKotlinArtifactsCommon(kgpVersion, containsAndroidTarget = false, enableSigning = false)
  }

  @TestParameterInjectorTest
  fun minimalPomProject() {
    val project = javaProjectSpec().copy(
      properties = emptyMap(),
    )
    val result = project.run()

    assertThat(result).hasSingleArtifactCommon(signed = false)
    assertThat(result).pom().matchesExpectedPom(modelFactory = ::createMinimalPom)
    assertThat(result).sourcesJar().containsAllSourceFiles()
  }

  @TestParameterInjectorTest
  fun groupAndVersionFromProjectProject() {
    val project = javaProjectSpec().copy(
      group = "",
      artifactId = "",
      version = "",
      buildFileExtra =
        """
        group = "com.example.test2"
        version = "3.2.1"
        """.trimIndent(),
    )
    val result = project.run()

    val resultSpec = project.copy(
      group = "com.example.test2",
      // the project name is used as default value for the artifact id
      artifactId = "module",
      version = "3.2.1",
    )
    val actualResult = result.copy(projectSpec = resultSpec)
    assertThat(actualResult).hasSingleArtifactCommon(signed = false)
    assertThat(actualResult).pom().matchesExpectedPom()
    assertThat(actualResult).sourcesJar().containsAllSourceFiles()
  }

  @TestParameterInjectorTest
  fun withoutSigning() {
    val project = javaProjectSpec()
    val result = project.run()

    assertThat(result).hasSingleArtifactCommon(signed = false)
  }

  @TestParameterInjectorTest
  fun signWithGpgKey() {
    val project = javaProjectSpec()
    val result = project.run(testOptions.copy(signing = GPG_KEY))

    assertThat(result).hasSingleArtifactCommon()
    assertThat(result).pom().matchesExpectedPom()
    assertThat(result).sourcesJar().containsAllSourceFiles()
  }
}
