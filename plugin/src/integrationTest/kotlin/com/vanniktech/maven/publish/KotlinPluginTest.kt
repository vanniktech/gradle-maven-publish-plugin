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
import com.vanniktech.maven.publish.util.javaTestFixturesPlugin
import com.vanniktech.maven.publish.util.kotlinJvmProjectSpec
import com.vanniktech.maven.publish.util.kotlinMultiplatformProjectSpec
import com.vanniktech.maven.publish.util.kotlinMultiplatformWithAndroidLibraryAndSpecifiedVariantsProjectSpec
import com.vanniktech.maven.publish.util.kotlinMultiplatformWithAndroidLibraryProjectSpec
import com.vanniktech.maven.publish.util.kotlinMultiplatformWithModernAndroidLibraryProjectSpec
import com.vanniktech.maven.publish.util.stdlibCommon

class KotlinPluginTest : BasePluginTest() {
  @TestParameterInjectorTest
  fun kotlinJvmProject(
    @TestParameter(valuesProvider = KgpVersionProvider::class) kgpVersion: KgpVersion,
  ) {
    kgpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = kotlinJvmProjectSpec(kgpVersion)
    val result = project.run()

    assertThat(result).hasSingleArtifactCommon()
    assertThat(result).pom().matchesExpectedPom(kgpVersion.stdlibCommon())
    assertThat(result).sourcesJar().containsAllSourceFiles()
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
    val result = project.run()

    assertThat(result).hasSingleArtifactCommon()
    assertThat(result).pom().matchesExpectedPom(kgpVersion.stdlibCommon())
    assertThat(result).sourcesJar().containsSourceSetFiles("main")
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
    val result = project.run()

    assertThat(result).hasKotlinArtifactsCommon(kgpVersion, containsAndroidTarget = false)
  }

  @TestParameterInjectorTest
  fun kotlinMultiplatformWithAndroidLibraryProject(
    @TestParameter(valuesProvider = AgpVersionProvider::class) agpVersion: AgpVersion,
    @TestParameter(valuesProvider = KgpVersionProvider::class) kgpVersion: KgpVersion,
  ) {
    agpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)
    kgpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = kotlinMultiplatformWithAndroidLibraryProjectSpec(agpVersion, kgpVersion)
    val result = project.run()

    val kotlinDependencyVersion = if (agpVersion >= AGP_9_0_0 && kgpVersion < KOTLIN_2_2_10) {
      KOTLIN_2_2_10
    } else {
      kgpVersion
    }

    assertThat(result).hasKotlinArtifactsCommon(kotlinDependencyVersion)
    result.withArtifactIdSuffix("android-debug").let { androidDebugResult ->
      assertThat(androidDebugResult).outcome().succeeded()
      assertThat(androidDebugResult).artifact("aar").doesNotExist()
      assertThat(androidDebugResult).pom().doesNotExist()
      assertThat(androidDebugResult).module().doesNotExist()
      assertThat(androidDebugResult).sourcesJar().doesNotExist()
      assertThat(androidDebugResult).javadocJar().doesNotExist()
    }
  }

  @TestParameterInjectorTest
  fun kotlinMultiplatformWithAndroidLibraryAndSpecifiedVariantsProject(
    @TestParameter(valuesProvider = AgpVersionProvider::class) agpVersion: AgpVersion,
    @TestParameter(valuesProvider = KgpVersionProvider::class) kgpVersion: KgpVersion,
  ) {
    agpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)
    kgpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = kotlinMultiplatformWithAndroidLibraryAndSpecifiedVariantsProjectSpec(agpVersion, kgpVersion)
    val result = project.run()

    val kotlinDependencyVersion = if (agpVersion >= AGP_9_0_0 && kgpVersion < KOTLIN_2_2_10) {
      KOTLIN_2_2_10
    } else {
      kgpVersion
    }

    assertThat(result).hasKotlinArtifactsCommon(kotlinDependencyVersion)
    result.withArtifactIdSuffix("android-debug").let { androidDebugResult ->
      assertThat(androidDebugResult).hasSingleArtifactCommon("aar")
      assertThat(androidDebugResult).pom().matchesExpectedPom("aar", kotlinDependencyVersion.stdlibCommon())
      assertThat(androidDebugResult).sourcesJar().containsSourceSetFiles("commonMain", "androidMain", "androidDebug")
    }
  }

  @TestParameterInjectorTest
  fun kotlinMultiplatformWithModernAndroidLibraryProject(
    @TestParameter(valuesProvider = AgpVersionProvider::class) agpVersion: AgpVersion,
    @TestParameter(valuesProvider = KgpVersionProvider::class) kgpVersion: KgpVersion,
  ) {
    agpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)
    kgpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = kotlinMultiplatformWithModernAndroidLibraryProjectSpec(agpVersion, kgpVersion)
    val result = project.run()

    val kotlinDependencyVersion = if (agpVersion >= AGP_9_0_0 && kgpVersion < KOTLIN_2_2_10) {
      KOTLIN_2_2_10
    } else {
      kgpVersion
    }

    assertThat(result).hasKotlinArtifactsCommon(kotlinDependencyVersion)
  }
}
