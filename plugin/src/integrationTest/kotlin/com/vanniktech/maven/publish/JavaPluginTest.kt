package com.vanniktech.maven.publish

import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import com.vanniktech.maven.publish.util.KgpVersion
import com.vanniktech.maven.publish.util.KgpVersionProvider
import com.vanniktech.maven.publish.util.PluginPublishVersion
import com.vanniktech.maven.publish.util.PluginPublishVersionProvider
import com.vanniktech.maven.publish.util.PomDependency
import com.vanniktech.maven.publish.util.ProjectResult
import com.vanniktech.maven.publish.util.ProjectResultSubject.Companion.assertThat
import com.vanniktech.maven.publish.util.SourceFile
import com.vanniktech.maven.publish.util.assumeSupportedJdkAndGradleVersion
import com.vanniktech.maven.publish.util.javaGradlePluginKotlinProjectSpec
import com.vanniktech.maven.publish.util.javaGradlePluginProjectSpec
import com.vanniktech.maven.publish.util.javaGradlePluginWithGradlePluginPublish
import com.vanniktech.maven.publish.util.javaLibraryProjectSpec
import com.vanniktech.maven.publish.util.javaPlatformProjectSpec
import com.vanniktech.maven.publish.util.javaProjectSpec
import com.vanniktech.maven.publish.util.javaTestFixturesPlugin
import com.vanniktech.maven.publish.util.stdlibCommon
import com.vanniktech.maven.publish.util.versionCatalogProjectSpec

class JavaPluginTest : BasePluginTest() {
  @TestParameterInjectorTest
  fun javaProject() {
    val project = javaProjectSpec()
    val result = project.run()

    assertSingleJarCommon(result)
  }

  @TestParameterInjectorTest
  fun javaLibraryProject() {
    val project = javaLibraryProjectSpec()
    val result = project.run()

    assertSingleJarCommon(result)
  }

  @TestParameterInjectorTest
  fun javaLibraryWithTestFixturesProject() {
    val default = javaLibraryProjectSpec()
    val project = default.copy(
      plugins = default.plugins + javaTestFixturesPlugin,
      sourceFiles = default.sourceFiles +
        SourceFile("testFixtures", "java", "com/vanniktech/maven/publish/test/TestFixtureClass.java"),
    )
    val result = project.run()

    assertThat(result).hasSingleArtifactCommon()
    assertThat(result).pom().matchesExpectedPom()
    assertThat(result).sourcesJar().containsSourceSetFiles("main")
    assertThat(result).artifact("test-fixtures", "jar").exists()
    assertThat(result).artifact("test-fixtures", "jar").isSigned()
    assertThat(result).sourcesJar("test-fixtures").exists()
    assertThat(result).sourcesJar("test-fixtures").isSigned()
    assertThat(result).sourcesJar("test-fixtures").containsSourceSetFiles("testFixtures")
  }

  @TestParameterInjectorTest
  fun javaGradlePluginProject() {
    val project = javaGradlePluginProjectSpec()
    val result = project.run()

    assertSingleJarCommon(result)

    val pluginId = "com.example.test-plugin"
    val pluginMarkerSpec = project.copy(group = pluginId, artifactId = "$pluginId.gradle.plugin")
    val pluginMarkerResult = result.copy(projectSpec = pluginMarkerSpec)
    assertThat(pluginMarkerResult).pom().exists()
    assertThat(pluginMarkerResult).pom().isSigned()
    assertThat(pluginMarkerResult).pom().matchesExpectedPom(
      "pom",
      PomDependency("com.example", "test-artifact", "1.0.0", null),
    )
  }

  @TestParameterInjectorTest
  fun javaGradlePluginWithPluginPublishProject(
    @TestParameter(valuesProvider = PluginPublishVersionProvider::class) pluginPublishVersion: PluginPublishVersion,
  ) {
    val project = javaGradlePluginWithGradlePluginPublish(pluginPublishVersion)
    val result = project.run()

    assertSingleJarCommon(result)

    val pluginId = "com.example.test-plugin"
    val pluginMarkerSpec = project.copy(group = pluginId, artifactId = "$pluginId.gradle.plugin")
    val pluginMarkerResult = result.copy(projectSpec = pluginMarkerSpec)
    assertThat(pluginMarkerResult).pom().exists()
    assertThat(pluginMarkerResult).pom().isSigned()
    assertThat(pluginMarkerResult).pom().matchesExpectedPom(
      "pom",
      PomDependency("com.example", "test-artifact", "1.0.0", null),
    )
  }

  @TestParameterInjectorTest
  fun javaGradlePluginKotlinProject(
    @TestParameter(valuesProvider = KgpVersionProvider::class) kgpVersion: KgpVersion,
  ) {
    kgpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = javaGradlePluginKotlinProjectSpec(kgpVersion)
    val result = project.run()

    assertThat(result).hasSingleArtifactCommon()
    assertThat(result).pom().matchesExpectedPom(kgpVersion.stdlibCommon())
    assertThat(result).sourcesJar().containsAllSourceFiles()

    val pluginId = "com.example.test-plugin"
    val pluginMarkerSpec = project.copy(group = pluginId, artifactId = "$pluginId.gradle.plugin")
    val pluginMarkerResult = result.copy(projectSpec = pluginMarkerSpec)
    assertThat(pluginMarkerResult).pom().exists()
    assertThat(pluginMarkerResult).pom().isSigned()
    assertThat(pluginMarkerResult).pom().matchesExpectedPom(
      "pom",
      PomDependency("com.example", "test-artifact", "1.0.0", null),
    )
  }

  @TestParameterInjectorTest
  fun javaLibraryWithToolchainProject() {
    val project = javaLibraryProjectSpec().copy(
      buildFileExtra =
        """
        java {
          toolchain.languageVersion = JavaLanguageVersion.of(11)
        }
        """.trimIndent(),
    )
    val result = project.run()

    assertSingleJarCommon(result)
  }

  @TestParameterInjectorTest
  fun javaPlatformProject() {
    val project = javaPlatformProjectSpec()
    val result = project.run()

    assertThat(result).outcome().succeeded()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom(
      packaging = "pom",
      dependencyManagementDependencies = listOf(
        PomDependency("commons-httpclient", "commons-httpclient", "3.1", null),
        PomDependency("org.postgresql", "postgresql", "42.2.5", null),
      ),
    )
    assertThat(result).module().exists()
    assertThat(result).module().isSigned()
  }

  @TestParameterInjectorTest
  fun versionCatalogProject() {
    val project = versionCatalogProjectSpec()
    val result = project.run()

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("toml").exists()
    assertThat(result).artifact("toml").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom("toml")
    assertThat(result).module().exists()
    assertThat(result).module().isSigned()
  }

  private fun assertSingleJarCommon(result: ProjectResult) {
    assertThat(result).hasSingleArtifactCommon()
    assertThat(result).pom().matchesExpectedPom()
    assertThat(result).sourcesJar().containsAllSourceFiles()
  }
}
