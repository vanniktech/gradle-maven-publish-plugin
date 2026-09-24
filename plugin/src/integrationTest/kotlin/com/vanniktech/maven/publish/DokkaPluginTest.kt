package com.vanniktech.maven.publish

import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import com.vanniktech.maven.publish.util.KgpVersion
import com.vanniktech.maven.publish.util.ProjectResult
import com.vanniktech.maven.publish.util.ProjectResultSubject.Companion.assertThat
import com.vanniktech.maven.publish.util.TestOptions
import com.vanniktech.maven.publish.util.TestOptions.Signing.NO_SIGNING
import com.vanniktech.maven.publish.util.dokkaJavadocPlugin
import com.vanniktech.maven.publish.util.dokkaPlugin
import com.vanniktech.maven.publish.util.kotlinJvmProjectSpec
import com.vanniktech.maven.publish.util.stdlibCommon

class DokkaPluginTest : BasePluginTest() {
  override val testOptions get() = TestOptions(config, NO_SIGNING, gradleVersion)

  @TestParameterInjectorTest
  fun dokka() {
    val kgpVersion = KgpVersion.VERSIONS.last()
    val original = kotlinJvmProjectSpec(kgpVersion)
    val project = original.copy(
      plugins = original.plugins + dokkaPlugin,
      basePluginConfig = original.basePluginConfig.replace(
        "JavadocJar.Empty()",
        "JavadocJar.Dokka(\"dokkaGeneratePublicationHtml\")",
      ),
    )
    val result = project.run()

    assertSingleJarCommon(result, kgpVersion)
  }

  @TestParameterInjectorTest
  fun dokkaJavadoc() {
    val kgpVersion = KgpVersion.VERSIONS.last()
    val original = kotlinJvmProjectSpec(kgpVersion)
    val project = original.copy(
      plugins = original.plugins + dokkaJavadocPlugin,
      basePluginConfig = original.basePluginConfig.replace(
        "JavadocJar.Empty()",
        "JavadocJar.Dokka(\"dokkaGeneratePublicationJavadoc\")",
      ),
    )
    val result = project.run()

    assertSingleJarCommon(result, kgpVersion)
  }

  private fun assertSingleJarCommon(result: ProjectResult, kgpVersion: KgpVersion) {
    assertThat(result).hasSingleArtifactCommon(signed = false)
    assertThat(result).pom().matchesExpectedPom(kgpVersion.stdlibCommon())
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().containsFiles(ignoreAdditionalFiles = true, "index.html")
  }
}
