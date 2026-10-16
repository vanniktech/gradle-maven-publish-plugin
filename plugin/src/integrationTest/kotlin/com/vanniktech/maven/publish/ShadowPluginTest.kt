package com.vanniktech.maven.publish

import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import com.vanniktech.maven.publish.util.ProjectResultSubject.Companion.assertThat
import com.vanniktech.maven.publish.util.ShadowVersion
import com.vanniktech.maven.publish.util.ShadowVersionProvider
import com.vanniktech.maven.publish.util.TestOptions
import com.vanniktech.maven.publish.util.assumeSupportedJdkAndGradleVersion
import com.vanniktech.maven.publish.util.shadowProjectSpec

class ShadowPluginTest : BasePluginTest() {
  @TestParameterInjectorTest
  fun shadowProject(
    @TestParameter(valuesProvider = ShadowVersionProvider::class) shadowVersion: ShadowVersion,
  ) {
    shadowVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = shadowProjectSpec(shadowVersion)
    val result = project.run()

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("all", "jar").exists()
    assertThat(result).artifact("all", "jar").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom("pom")
    assertThat(result).module().exists()
    assertThat(result).module().isSigned()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSigned()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSigned()
  }

  @TestParameterInjectorTest
  fun shadowProjectWithEmptyClassifier(
    @TestParameter(valuesProvider = ShadowVersionProvider::class) shadowVersion: ShadowVersion,
  ) {
    shadowVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = shadowProjectSpec(shadowVersion).copy(
      buildFileExtra =
        """
        tasks.named('shadowJar', com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar) {
          archiveClassifier = ''
        }
        """.trimIndent(),
    )
    val result = project.run()

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

  @TestParameterInjectorTest
  fun shadowProjectWithJavaLibraryAndShadowVariant(
    @TestParameter(valuesProvider = ShadowVersionProvider::class) shadowVersion: ShadowVersion,
  ) {
    shadowVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = shadowProjectSpec(shadowVersion).copy(
      basePluginConfig = "configure(new JavaLibrary(new JavadocJar.Empty(), new SourcesJar.Sources()))",
      buildFileExtra =
        if (config == TestOptions.Config.BASE) {
          ""
        } else {
          """
          mavenPublishing {
            configure(new JavaLibrary(new JavadocJar.Empty(), new SourcesJar.Sources()))
          }
          """.trimIndent()
        },
    )
    val result = project.run()

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSigned()
    assertThat(result).artifact("all", "jar").exists()
    assertThat(result).artifact("all", "jar").isSigned()
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
