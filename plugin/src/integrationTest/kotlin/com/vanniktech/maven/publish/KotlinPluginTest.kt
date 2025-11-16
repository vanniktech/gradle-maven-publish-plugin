package com.vanniktech.maven.publish

import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import com.vanniktech.maven.publish.util.AgpVersion
import com.vanniktech.maven.publish.util.AgpVersion.Companion.AGP_9_0_0
import com.vanniktech.maven.publish.util.AgpVersionProvider
import com.vanniktech.maven.publish.util.KgpVersion
import com.vanniktech.maven.publish.util.KgpVersion.Companion.KOTLIN_2_2_10
import com.vanniktech.maven.publish.util.KgpVersionProvider
import com.vanniktech.maven.publish.util.ProjectResultSubject.Companion.assertThat
import com.vanniktech.maven.publish.util.SourceFile
import com.vanniktech.maven.publish.util.assumeSupportedJdkAndGradleVersion
import com.vanniktech.maven.publish.util.domApiCompat
import com.vanniktech.maven.publish.util.fixtures
import com.vanniktech.maven.publish.util.javaTestFixturesPlugin
import com.vanniktech.maven.publish.util.kotlinJvmProjectSpec
import com.vanniktech.maven.publish.util.kotlinMultiplatformProjectSpec
import com.vanniktech.maven.publish.util.kotlinMultiplatformWithAndroidLibraryAndSpecifiedVariantsProjectSpec
import com.vanniktech.maven.publish.util.kotlinMultiplatformWithAndroidLibraryProjectSpec
import com.vanniktech.maven.publish.util.kotlinMultiplatformWithModernAndroidLibraryProjectSpec
import com.vanniktech.maven.publish.util.run
import com.vanniktech.maven.publish.util.stdlibCommon
import com.vanniktech.maven.publish.util.stdlibJs

class KotlinPluginTest : BasePluginTest() {
  @TestParameterInjectorTest
  fun kotlinJvmProject(
    @TestParameter(valuesProvider = KgpVersionProvider::class) kgpVersion: KgpVersion,
  ) {
    kgpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = kotlinJvmProjectSpec(kgpVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom(kgpVersion.stdlibCommon())
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
    @TestParameter(valuesProvider = KgpVersionProvider::class) kgpVersion: KgpVersion,
  ) {
    kgpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val default = kotlinJvmProjectSpec(kgpVersion)
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
    assertThat(result).pom().matchesExpectedPom(kgpVersion.stdlibCommon())
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
    @TestParameter(valuesProvider = KgpVersionProvider::class) kgpVersion: KgpVersion,
  ) {
    kgpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = kotlinMultiplatformProjectSpec(kgpVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom(
      kgpVersion.stdlibCommon().copy(scope = "runtime"),
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
    assertThat(jvmResult).pom().matchesExpectedPom(kgpVersion.stdlibCommon())
    assertThat(jvmResult).module().exists()
    assertThat(jvmResult).module().isSigned()
    assertThat(jvmResult).sourcesJar().exists()
    assertThat(jvmResult).sourcesJar().isSigned()
    assertThat(jvmResult).sourcesJar().containsSourceSetFiles("commonMain", "jvmMain")
    assertThat(jvmResult).javadocJar().exists()
    assertThat(jvmResult).javadocJar().isSigned()

    val linuxResult = result.withArtifactIdSuffix("linuxx64")
    assertThat(linuxResult).outcome().succeeded()
    assertThat(linuxResult).artifact("klib").exists()
    assertThat(linuxResult).artifact("klib").isSigned()
    assertThat(linuxResult).pom().exists()
    assertThat(linuxResult).pom().isSigned()
    assertThat(linuxResult).pom().matchesExpectedPom("klib", kgpVersion.stdlibCommon())
    assertThat(linuxResult).module().exists()
    assertThat(linuxResult).module().isSigned()
    assertThat(linuxResult).sourcesJar().exists()
    assertThat(linuxResult).sourcesJar().isSigned()
    assertThat(linuxResult).sourcesJar().containsSourceSetFiles("commonMain", "linuxX64Main")
    assertThat(linuxResult).javadocJar().exists()
    assertThat(linuxResult).javadocJar().isSigned()

    val nodejsResult = result.withArtifactIdSuffix("nodejs")
    assertThat(nodejsResult).outcome().succeeded()
    assertThat(nodejsResult).artifact("klib").exists()
    assertThat(nodejsResult).artifact("klib").isSigned()
    assertThat(nodejsResult).pom().exists()
    assertThat(nodejsResult).pom().isSigned()
    assertThat(nodejsResult).pom().matchesExpectedPom("klib", kgpVersion.stdlibJs(), kgpVersion.domApiCompat())
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
    @TestParameter(valuesProvider = KgpVersionProvider::class) kgpVersion: KgpVersion,
  ) {
    agpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)
    kgpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = kotlinMultiplatformWithAndroidLibraryProjectSpec(agpVersion, kgpVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    val kotlinDependencyVersion = if (agpVersion >= AGP_9_0_0 && kgpVersion < KOTLIN_2_2_10) {
      KOTLIN_2_2_10
    } else {
      kgpVersion
    }

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom(
      kotlinDependencyVersion.stdlibCommon().copy(scope = "runtime"),
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
    assertThat(jvmResult).pom().matchesExpectedPom(kotlinDependencyVersion.stdlibCommon())
    assertThat(jvmResult).module().exists()
    assertThat(jvmResult).module().isSigned()
    assertThat(jvmResult).sourcesJar().exists()
    assertThat(jvmResult).sourcesJar().isSigned()
    assertThat(jvmResult).sourcesJar().containsSourceSetFiles("commonMain", "jvmMain")
    assertThat(jvmResult).javadocJar().exists()
    assertThat(jvmResult).javadocJar().isSigned()

    val linuxResult = result.withArtifactIdSuffix("linuxx64")
    assertThat(linuxResult).outcome().succeeded()
    assertThat(linuxResult).artifact("klib").exists()
    assertThat(linuxResult).artifact("klib").isSigned()
    assertThat(linuxResult).pom().exists()
    assertThat(linuxResult).pom().isSigned()
    assertThat(linuxResult).pom().matchesExpectedPom("klib", kotlinDependencyVersion.stdlibCommon())
    assertThat(linuxResult).module().exists()
    assertThat(linuxResult).module().isSigned()
    assertThat(linuxResult).sourcesJar().exists()
    assertThat(linuxResult).sourcesJar().isSigned()
    assertThat(linuxResult).sourcesJar().containsSourceSetFiles("commonMain", "linuxX64Main")
    assertThat(linuxResult).javadocJar().exists()
    assertThat(linuxResult).javadocJar().isSigned()

    val nodejsResult = result.withArtifactIdSuffix("nodejs")
    assertThat(nodejsResult).outcome().succeeded()
    assertThat(nodejsResult).artifact("klib").exists()
    assertThat(nodejsResult).artifact("klib").isSigned()
    assertThat(nodejsResult).pom().exists()
    assertThat(nodejsResult).pom().isSigned()
    assertThat(
      nodejsResult,
    ).pom().matchesExpectedPom("klib", kotlinDependencyVersion.stdlibJs(), kotlinDependencyVersion.domApiCompat())
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
    assertThat(androidReleaseResult).pom().matchesExpectedPom("aar", kotlinDependencyVersion.stdlibCommon())
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
    @TestParameter(valuesProvider = KgpVersionProvider::class) kgpVersion: KgpVersion,
  ) {
    agpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)
    kgpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = kotlinMultiplatformWithAndroidLibraryAndSpecifiedVariantsProjectSpec(agpVersion, kgpVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    val kotlinDependencyVersion = if (agpVersion >= AGP_9_0_0 && kgpVersion < KOTLIN_2_2_10) {
      KOTLIN_2_2_10
    } else {
      kgpVersion
    }

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom(kotlinDependencyVersion.stdlibCommon().copy(scope = "runtime"))
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
    assertThat(jvmResult).pom().matchesExpectedPom(kotlinDependencyVersion.stdlibCommon())
    assertThat(jvmResult).module().exists()
    assertThat(jvmResult).module().isSigned()
    assertThat(jvmResult).sourcesJar().exists()
    assertThat(jvmResult).sourcesJar().isSigned()
    assertThat(jvmResult).sourcesJar().containsSourceSetFiles("commonMain", "jvmMain")
    assertThat(jvmResult).javadocJar().exists()
    assertThat(jvmResult).javadocJar().isSigned()

    val linuxResult = result.withArtifactIdSuffix("linuxx64")
    assertThat(linuxResult).outcome().succeeded()
    assertThat(linuxResult).artifact("klib").exists()
    assertThat(linuxResult).artifact("klib").isSigned()
    assertThat(linuxResult).pom().exists()
    assertThat(linuxResult).pom().isSigned()
    assertThat(linuxResult).pom().matchesExpectedPom("klib", kotlinDependencyVersion.stdlibCommon())
    assertThat(linuxResult).module().exists()
    assertThat(linuxResult).module().isSigned()
    assertThat(linuxResult).sourcesJar().exists()
    assertThat(linuxResult).sourcesJar().isSigned()
    assertThat(linuxResult).sourcesJar().containsSourceSetFiles("commonMain", "linuxX64Main")
    assertThat(linuxResult).javadocJar().exists()
    assertThat(linuxResult).javadocJar().isSigned()

    val nodejsResult = result.withArtifactIdSuffix("nodejs")
    assertThat(nodejsResult).outcome().succeeded()
    assertThat(nodejsResult).artifact("klib").exists()
    assertThat(nodejsResult).artifact("klib").isSigned()
    assertThat(nodejsResult).pom().exists()
    assertThat(nodejsResult).pom().isSigned()
    assertThat(
      nodejsResult,
    ).pom().matchesExpectedPom("klib", kotlinDependencyVersion.stdlibJs(), kotlinDependencyVersion.domApiCompat())
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
    assertThat(androidReleaseResult).pom().matchesExpectedPom("aar", kotlinDependencyVersion.stdlibCommon())
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
    assertThat(androidDebugResult).pom().matchesExpectedPom("aar", kotlinDependencyVersion.stdlibCommon())
    assertThat(androidDebugResult).module().exists()
    assertThat(androidDebugResult).module().isSigned()
    assertThat(androidDebugResult).sourcesJar().exists()
    assertThat(androidDebugResult).sourcesJar().isSigned()
    assertThat(androidDebugResult).sourcesJar().containsSourceSetFiles("commonMain", "androidMain", "androidDebug")
    assertThat(androidDebugResult).javadocJar().exists()
    assertThat(androidDebugResult).javadocJar().isSigned()
  }

  @TestParameterInjectorTest
  fun kotlinMultiplatformWithModernAndroidLibraryProject(
    @TestParameter(valuesProvider = AgpVersionProvider::class) agpVersion: AgpVersion,
    @TestParameter(valuesProvider = KgpVersionProvider::class) kgpVersion: KgpVersion,
  ) {
    agpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)
    kgpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = kotlinMultiplatformWithModernAndroidLibraryProjectSpec(agpVersion, kgpVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    val kotlinDependencyVersion = if (agpVersion >= AGP_9_0_0 && kgpVersion < KOTLIN_2_2_10) {
      KOTLIN_2_2_10
    } else {
      kgpVersion
    }

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("jar").exists()
    assertThat(result).artifact("jar").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom(
      kotlinDependencyVersion.stdlibCommon().copy(scope = "runtime"),
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
    assertThat(jvmResult).pom().matchesExpectedPom(kotlinDependencyVersion.stdlibCommon())
    assertThat(jvmResult).module().exists()
    assertThat(jvmResult).module().isSigned()
    assertThat(jvmResult).sourcesJar().exists()
    assertThat(jvmResult).sourcesJar().isSigned()
    assertThat(jvmResult).sourcesJar().containsSourceSetFiles("commonMain", "jvmMain")
    assertThat(jvmResult).javadocJar().exists()
    assertThat(jvmResult).javadocJar().isSigned()

    val linuxResult = result.withArtifactIdSuffix("linuxx64")
    assertThat(linuxResult).outcome().succeeded()
    assertThat(linuxResult).artifact("klib").exists()
    assertThat(linuxResult).artifact("klib").isSigned()
    assertThat(linuxResult).pom().exists()
    assertThat(linuxResult).pom().isSigned()
    assertThat(linuxResult).pom().matchesExpectedPom("klib", kotlinDependencyVersion.stdlibCommon())
    assertThat(linuxResult).module().exists()
    assertThat(linuxResult).module().isSigned()
    assertThat(linuxResult).sourcesJar().exists()
    assertThat(linuxResult).sourcesJar().isSigned()
    assertThat(linuxResult).sourcesJar().containsSourceSetFiles("commonMain", "linuxX64Main")
    assertThat(linuxResult).javadocJar().exists()
    assertThat(linuxResult).javadocJar().isSigned()

    val nodejsResult = result.withArtifactIdSuffix("nodejs")
    assertThat(nodejsResult).outcome().succeeded()
    assertThat(nodejsResult).artifact("klib").exists()
    assertThat(nodejsResult).artifact("klib").isSigned()
    assertThat(nodejsResult).pom().exists()
    assertThat(nodejsResult).pom().isSigned()
    assertThat(
      nodejsResult,
    ).pom().matchesExpectedPom("klib", kotlinDependencyVersion.stdlibJs(), kotlinDependencyVersion.domApiCompat())
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
    assertThat(androidReleaseResult).pom().matchesExpectedPom("aar", kotlinDependencyVersion.stdlibCommon())
    assertThat(androidReleaseResult).module().exists()
    assertThat(androidReleaseResult).module().isSigned()
    assertThat(androidReleaseResult).sourcesJar().exists()
    assertThat(androidReleaseResult).sourcesJar().isSigned()
    assertThat(androidReleaseResult).sourcesJar().containsSourceSetFiles("commonMain", "androidMain", "androidRelease")
    assertThat(androidReleaseResult).javadocJar().exists()
    assertThat(androidReleaseResult).javadocJar().isSigned()
  }
}
