package com.vanniktech.maven.publish

import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import com.vanniktech.maven.publish.ProjectResultSubject.Companion.assertThat

class KotlinPluginTest : BasePluginTest() {
  @TestParameterInjectorTest
  fun kotlinJvmProject(
    @TestParameter(valuesProvider = KotlinVersionProvider::class) kotlinVersion: KotlinVersion,
  ) {
    kotlinVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = kotlinJvmProjectSpec(kotlinVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom(kotlinStdlibJdk(kotlinVersion))
    assertThat(result).module().exists()
    assertThat(result).module().isSigned()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSigned()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSigned()
  }

  @TestParameterInjectorTest
  fun kotlinJvmWithTestFixturesProject(
    @TestParameter(valuesProvider = KotlinVersionProvider::class) kotlinVersion: KotlinVersion,
  ) {
    kotlinVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val default = kotlinJvmProjectSpec(kotlinVersion)
    val project = default.copy(
      plugins = default.plugins + javaTestFixturesPlugin,
      sourceFiles = default.sourceFiles + listOf(
        SourceFile("testFixtures", "java", "com/vanniktech/maven/publish/test/TestFixtureClass.java"),
        SourceFile("testFixtures", "kotlin", "com/vanniktech/maven/publish/test/TestFixtureKotlinClass.kt"),
      ),
    )
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom(kotlinStdlibJdk(kotlinVersion))
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
  fun kotlinMultiplatformProject(
    @TestParameter(valuesProvider = KotlinVersionProvider::class) kotlinVersion: KotlinVersion,
  ) {
    kotlinVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = kotlinMultiplatformProjectSpec(kotlinVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom(
      kotlinStdlibCommon(kotlinVersion).copy(scope = "runtime"),
    )
    assertThat(result).module().exists()
    assertThat(result).module().isSigned()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSigned()
    assertThat(result).sourcesJar().containsSourceSetFiles("commonMain")
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSigned()

    val jvmResult = result.withArtifactIdSuffix("jvm")
    assertThat(jvmResult).outcome().succeeded()
    assertThat(jvmResult).artifact("jar").exists()
    assertThat(jvmResult).artifact("jar").isSigned()
    assertThat(jvmResult).pom().exists()
    assertThat(jvmResult).pom().isSigned()
    assertThat(jvmResult).pom().matchesExpectedPom(
      kotlinStdlibJdk(kotlinVersion),
      kotlinStdlibCommon(kotlinVersion),
    )
    assertThat(jvmResult).module().exists()
    assertThat(jvmResult).module().isSigned()
    assertThat(jvmResult).sourcesJar().exists()
    assertThat(jvmResult).sourcesJar().isSigned()
    assertThat(jvmResult).sourcesJar().containsSourceSetFiles("commonMain", "jvmMain")
    assertThat(jvmResult).javadocJar().exists()
    assertThat(jvmResult).javadocJar().isSigned()

    val linuxResult = result.withArtifactIdSuffix("linux")
    assertThat(linuxResult).outcome().succeeded()
    assertThat(linuxResult).artifact("klib").exists()
    assertThat(linuxResult).artifact("klib").isSigned()
    assertThat(linuxResult).pom().exists()
    assertThat(linuxResult).pom().isSigned()
    assertThat(linuxResult).pom().matchesExpectedPom(
      "klib",
      kotlinStdlibCommon(kotlinVersion),
    )
    assertThat(linuxResult).module().exists()
    assertThat(linuxResult).module().isSigned()
    assertThat(linuxResult).sourcesJar().exists()
    assertThat(linuxResult).sourcesJar().isSigned()
    assertThat(linuxResult).sourcesJar().containsSourceSetFiles("commonMain", "linuxMain")
    assertThat(linuxResult).javadocJar().exists()
    assertThat(linuxResult).javadocJar().isSigned()

    val nodejsResult = result.withArtifactIdSuffix("nodejs")
    assertThat(nodejsResult).outcome().succeeded()
    assertThat(nodejsResult).artifact("klib").exists()
    assertThat(nodejsResult).artifact("klib").isSigned()
    assertThat(nodejsResult).pom().exists()
    assertThat(nodejsResult).pom().isSigned()
    assertThat(nodejsResult).pom().matchesExpectedPom("klib", kotlinStdlibJs(kotlinVersion), kotlinDomApi(kotlinVersion))
    assertThat(nodejsResult).module().exists()
    assertThat(nodejsResult).module().isSigned()
    assertThat(nodejsResult).sourcesJar().exists()
    assertThat(nodejsResult).sourcesJar().isSigned()
    assertThat(nodejsResult).sourcesJar().containsSourceSetFiles("commonMain", "nodeJsMain")
    assertThat(nodejsResult).javadocJar().exists()
    assertThat(nodejsResult).javadocJar().isSigned()
  }

  @TestParameterInjectorTest
  fun kotlinMultiplatformWithAndroidLibraryProject(
    @TestParameter(valuesProvider = AgpVersionProvider::class) agpVersion: AgpVersion,
    @TestParameter(valuesProvider = KotlinVersionProvider::class) kotlinVersion: KotlinVersion,
  ) {
    agpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)
    kotlinVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = kotlinMultiplatformWithAndroidLibraryProjectSpec(agpVersion, kotlinVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom(
      kotlinStdlibCommon(kotlinVersion).copy(scope = "runtime"),
    )
    assertThat(result).module().exists()
    assertThat(result).module().isSigned()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSigned()
    assertThat(result).sourcesJar().containsSourceSetFiles("commonMain")
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSigned()

    val jvmResult = result.withArtifactIdSuffix("jvm")
    assertThat(jvmResult).outcome().succeeded()
    assertThat(jvmResult).artifact("jar").exists()
    assertThat(jvmResult).artifact("jar").isSigned()
    assertThat(jvmResult).pom().exists()
    assertThat(jvmResult).pom().isSigned()
    assertThat(jvmResult).pom().matchesExpectedPom(
      kotlinStdlibJdk(kotlinVersion),
      kotlinStdlibCommon(kotlinVersion),
    )
    assertThat(jvmResult).module().exists()
    assertThat(jvmResult).module().isSigned()
    assertThat(jvmResult).sourcesJar().exists()
    assertThat(jvmResult).sourcesJar().isSigned()
    assertThat(jvmResult).sourcesJar().containsSourceSetFiles("commonMain", "jvmMain")
    assertThat(jvmResult).javadocJar().exists()
    assertThat(jvmResult).javadocJar().isSigned()

    val linuxResult = result.withArtifactIdSuffix("linux")
    assertThat(linuxResult).outcome().succeeded()
    assertThat(linuxResult).artifact("klib").exists()
    assertThat(linuxResult).artifact("klib").isSigned()
    assertThat(linuxResult).pom().exists()
    assertThat(linuxResult).pom().isSigned()
    assertThat(linuxResult).pom().matchesExpectedPom(
      "klib",
      kotlinStdlibCommon(kotlinVersion),
    )
    assertThat(linuxResult).module().exists()
    assertThat(linuxResult).module().isSigned()
    assertThat(linuxResult).sourcesJar().exists()
    assertThat(linuxResult).sourcesJar().isSigned()
    assertThat(linuxResult).sourcesJar().containsSourceSetFiles("commonMain", "linuxMain")
    assertThat(linuxResult).javadocJar().exists()
    assertThat(linuxResult).javadocJar().isSigned()

    val nodejsResult = result.withArtifactIdSuffix("nodejs")
    assertThat(nodejsResult).outcome().succeeded()
    assertThat(nodejsResult).artifact("klib").exists()
    assertThat(nodejsResult).artifact("klib").isSigned()
    assertThat(nodejsResult).pom().exists()
    assertThat(nodejsResult).pom().isSigned()
    assertThat(nodejsResult).pom().matchesExpectedPom("klib", kotlinStdlibJs(kotlinVersion), kotlinDomApi(kotlinVersion))
    assertThat(nodejsResult).module().exists()
    assertThat(nodejsResult).module().isSigned()
    assertThat(nodejsResult).sourcesJar().exists()
    assertThat(nodejsResult).sourcesJar().isSigned()
    assertThat(nodejsResult).sourcesJar().containsSourceSetFiles("commonMain", "nodeJsMain")
    assertThat(nodejsResult).javadocJar().exists()
    assertThat(nodejsResult).javadocJar().isSigned()

    val androidReleaseResult = result.withArtifactIdSuffix("android")
    assertThat(androidReleaseResult).outcome().succeeded()
    assertThat(androidReleaseResult).artifact("aar").exists()
    assertThat(androidReleaseResult).artifact("aar").isSigned()
    assertThat(androidReleaseResult).pom().exists()
    assertThat(androidReleaseResult).pom().isSigned()
    assertThat(androidReleaseResult).pom().matchesExpectedPom(
      "aar",
      kotlinStdlibJdk(kotlinVersion),
      kotlinStdlibCommon(kotlinVersion),
    )
    assertThat(androidReleaseResult).module().exists()
    assertThat(androidReleaseResult).module().isSigned()
    assertThat(androidReleaseResult).sourcesJar().exists()
    assertThat(androidReleaseResult).sourcesJar().isSigned()
    assertThat(androidReleaseResult).sourcesJar().containsSourceSetFiles("commonMain", "androidMain", "androidRelease")
    assertThat(androidReleaseResult).javadocJar().exists()
    assertThat(androidReleaseResult).javadocJar().isSigned()

    val androidDebugResult = result.withArtifactIdSuffix("android-debug")
    assertThat(androidDebugResult).outcome().succeeded()
    assertThat(androidDebugResult).artifact("aar").doesNotExist()
    assertThat(androidDebugResult).pom().doesNotExist()
    assertThat(androidDebugResult).module().doesNotExist()
    assertThat(androidDebugResult).sourcesJar().doesNotExist()
    assertThat(androidDebugResult).javadocJar().doesNotExist()
  }

  @TestParameterInjectorTest
  fun kotlinMultiplatformWithAndroidLibraryAndSpecifiedVariantsProject(
    @TestParameter(valuesProvider = AgpVersionProvider::class) agpVersion: AgpVersion,
    @TestParameter(valuesProvider = KotlinVersionProvider::class) kotlinVersion: KotlinVersion,
  ) {
    agpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)
    kotlinVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = kotlinMultiplatformWithAndroidLibraryAndSpecifiedVariantsProjectSpec(agpVersion, kotlinVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom(
      kotlinStdlibCommon(kotlinVersion).copy(scope = "runtime"),
    )
    assertThat(result).module().exists()
    assertThat(result).module().isSigned()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSigned()
    assertThat(result).sourcesJar().containsSourceSetFiles("commonMain")
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSigned()

    val jvmResult = result.withArtifactIdSuffix("jvm")
    assertThat(jvmResult).outcome().succeeded()
    assertThat(jvmResult).artifact("jar").exists()
    assertThat(jvmResult).artifact("jar").isSigned()
    assertThat(jvmResult).pom().exists()
    assertThat(jvmResult).pom().isSigned()
    assertThat(jvmResult).pom().matchesExpectedPom(
      kotlinStdlibJdk(kotlinVersion),
      kotlinStdlibCommon(kotlinVersion),
    )
    assertThat(jvmResult).module().exists()
    assertThat(jvmResult).module().isSigned()
    assertThat(jvmResult).sourcesJar().exists()
    assertThat(jvmResult).sourcesJar().isSigned()
    assertThat(jvmResult).sourcesJar().containsSourceSetFiles("commonMain", "jvmMain")
    assertThat(jvmResult).javadocJar().exists()
    assertThat(jvmResult).javadocJar().isSigned()

    val linuxResult = result.withArtifactIdSuffix("linux")
    assertThat(linuxResult).outcome().succeeded()
    assertThat(linuxResult).artifact("klib").exists()
    assertThat(linuxResult).artifact("klib").isSigned()
    assertThat(linuxResult).pom().exists()
    assertThat(linuxResult).pom().isSigned()
    assertThat(linuxResult).pom().matchesExpectedPom(
      "klib",
      kotlinStdlibCommon(kotlinVersion),
    )
    assertThat(linuxResult).module().exists()
    assertThat(linuxResult).module().isSigned()
    assertThat(linuxResult).sourcesJar().exists()
    assertThat(linuxResult).sourcesJar().isSigned()
    assertThat(linuxResult).sourcesJar().containsSourceSetFiles("commonMain", "linuxMain")
    assertThat(linuxResult).javadocJar().exists()
    assertThat(linuxResult).javadocJar().isSigned()

    val nodejsResult = result.withArtifactIdSuffix("nodejs")
    assertThat(nodejsResult).outcome().succeeded()
    assertThat(nodejsResult).artifact("klib").exists()
    assertThat(nodejsResult).artifact("klib").isSigned()
    assertThat(nodejsResult).pom().exists()
    assertThat(nodejsResult).pom().isSigned()
    assertThat(nodejsResult).pom().matchesExpectedPom("klib", kotlinStdlibJs(kotlinVersion), kotlinDomApi(kotlinVersion))
    assertThat(nodejsResult).module().exists()
    assertThat(nodejsResult).module().isSigned()
    assertThat(nodejsResult).sourcesJar().exists()
    assertThat(nodejsResult).sourcesJar().isSigned()
    assertThat(nodejsResult).sourcesJar().containsSourceSetFiles("commonMain", "nodeJsMain")
    assertThat(nodejsResult).javadocJar().exists()
    assertThat(nodejsResult).javadocJar().isSigned()

    val androidReleaseResult = result.withArtifactIdSuffix("android")
    assertThat(androidReleaseResult).outcome().succeeded()
    assertThat(androidReleaseResult).artifact("aar").exists()
    assertThat(androidReleaseResult).artifact("aar").isSigned()
    assertThat(androidReleaseResult).pom().exists()
    assertThat(androidReleaseResult).pom().isSigned()
    assertThat(androidReleaseResult).pom().matchesExpectedPom(
      "aar",
      kotlinStdlibJdk(kotlinVersion),
      kotlinStdlibCommon(kotlinVersion),
    )
    assertThat(androidReleaseResult).module().exists()
    assertThat(androidReleaseResult).module().isSigned()
    assertThat(androidReleaseResult).sourcesJar().exists()
    assertThat(androidReleaseResult).sourcesJar().isSigned()
    assertThat(androidReleaseResult).sourcesJar().containsSourceSetFiles("commonMain", "androidMain", "androidRelease")
    assertThat(androidReleaseResult).javadocJar().exists()
    assertThat(androidReleaseResult).javadocJar().isSigned()

    val androidDebugResult = result.withArtifactIdSuffix("android-debug")
    assertThat(androidDebugResult).outcome().succeeded()
    assertThat(androidDebugResult).artifact("aar").exists()
    assertThat(androidDebugResult).artifact("aar").isSigned()
    assertThat(androidDebugResult).pom().exists()
    assertThat(androidDebugResult).pom().isSigned()
    assertThat(androidDebugResult).pom().matchesExpectedPom(
      "aar",
      kotlinStdlibJdk(kotlinVersion),
      kotlinStdlibCommon(kotlinVersion),
    )
    assertThat(androidDebugResult).module().exists()
    assertThat(androidDebugResult).module().isSigned()
    assertThat(androidDebugResult).sourcesJar().exists()
    assertThat(androidDebugResult).sourcesJar().isSigned()
    assertThat(androidDebugResult).sourcesJar().containsSourceSetFiles("commonMain", "androidMain", "androidDebug")
    assertThat(androidDebugResult).javadocJar().exists()
    assertThat(androidDebugResult).javadocJar().isSigned()
  }
}
