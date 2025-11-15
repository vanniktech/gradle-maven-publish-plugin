package com.vanniktech.maven.publish

import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import com.vanniktech.maven.publish.ProjectResultSubject.Companion.assertThat

class JavaPluginTest : BasePluginTest() {
  @TestParameterInjectorTest
  fun javaProject() {
    val project = javaProjectSpec()
    val result = project.run(fixtures, testProjectDir, testOptions)

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
  fun javaLibraryProject() {
    val project = javaLibraryProjectSpec()
    val result = project.run(fixtures, testProjectDir, testOptions)

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
  fun javaLibraryWithTestFixturesProject() {
    val default = javaLibraryProjectSpec()
    val project = default.copy(
      plugins = default.plugins + javaTestFixturesPlugin,
      sourceFiles = default.sourceFiles +
        SourceFile("testFixtures", "java", "com/vanniktech/maven/publish/test/TestFixtureClass.java"),
    )
    val result = project.run(fixtures, testProjectDir, testOptions)

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
    assertThat(result).sourcesJar().containsSourceSetFiles("main")
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSigned()
    assertThat(result).artifact("test-fixtures", "jar").exists()
    assertThat(result).artifact("test-fixtures", "jar").isSigned()
    assertThat(result).sourcesJar("test-fixtures").exists()
    assertThat(result).sourcesJar("test-fixtures").isSigned()
    assertThat(result).sourcesJar("test-fixtures").containsSourceSetFiles("testFixtures")
  }

  @TestParameterInjectorTest
  fun javaGradlePluginProject() {
    val project = javaGradlePluginProjectSpec()
    val result = project.run(fixtures, testProjectDir, testOptions)

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
    val result = project.run(fixtures, testProjectDir, testOptions)

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
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom(kotlinStdlibJdk(kgpVersion))
    assertThat(result).module().exists()
    assertThat(result).module().isSigned()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSigned()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSigned()

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
    val result = project.run(fixtures, testProjectDir, testOptions)

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
  fun javaPlatformProject() {
    val project = javaPlatformProjectSpec()
    val result = project.run(fixtures, testProjectDir, testOptions)

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
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("toml").exists()
    assertThat(result).artifact("toml").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom("toml")
    assertThat(result).module().exists()
    assertThat(result).module().isSigned()
  }
}
