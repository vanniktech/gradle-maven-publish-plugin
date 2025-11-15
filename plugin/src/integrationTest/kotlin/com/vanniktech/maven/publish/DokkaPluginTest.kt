package com.vanniktech.maven.publish

import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import com.vanniktech.maven.publish.ProjectResultSubject.Companion.assertThat
import com.vanniktech.maven.publish.TestOptions.Signing.NO_SIGNING
import org.junit.jupiter.api.condition.DisabledOnJre
import org.junit.jupiter.api.condition.JRE

class DokkaPluginTest : BasePluginTest() {
  override val testOptions get() = TestOptions(config, NO_SIGNING, gradleVersion)

  @DisabledOnJre(
    value = [JRE.JAVA_25],
    disabledReason = "Dokka 1.x does not support Java 25+.",
  )
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
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).pom().exists()
    assertThat(result).pom().matchesExpectedPom(kotlinStdlibJdk(kgpVersion))
    assertThat(result).module().exists()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().containsFiles(ignoreAdditionalFiles = true, "index.html")
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
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).pom().exists()
    assertThat(result).pom().matchesExpectedPom(kotlinStdlibJdk(kgpVersion))
    assertThat(result).module().exists()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().containsFiles(ignoreAdditionalFiles = true, "index.html")
  }
}
