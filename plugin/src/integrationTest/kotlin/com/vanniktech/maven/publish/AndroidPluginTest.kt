package com.vanniktech.maven.publish

import com.google.common.truth.TruthJUnit.assume
import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import com.vanniktech.maven.publish.ProjectResultSubject.Companion.assertThat
import com.vanniktech.maven.publish.util.AgpVersion
import com.vanniktech.maven.publish.util.AgpVersion.Companion.AGP_9_0_0
import com.vanniktech.maven.publish.util.AgpVersionProvider
import com.vanniktech.maven.publish.util.KgpVersion
import com.vanniktech.maven.publish.util.KgpVersion.Companion.KOTLIN_2_2_10
import com.vanniktech.maven.publish.util.KgpVersionProvider
import com.vanniktech.maven.publish.util.TestOptions
import com.vanniktech.maven.publish.util.androidFusedLibraryProjectSpec
import com.vanniktech.maven.publish.util.androidLibraryKotlinProjectSpec
import com.vanniktech.maven.publish.util.androidLibraryProjectSpec
import com.vanniktech.maven.publish.util.assumeSupportedJdkAndGradleVersion
import com.vanniktech.maven.publish.util.fixtures
import com.vanniktech.maven.publish.util.kotlinStdlibCommon
import com.vanniktech.maven.publish.util.run
import org.junit.jupiter.api.condition.DisabledOnJre
import org.junit.jupiter.api.condition.JRE

class AndroidPluginTest : BasePluginTest() {
  @TestParameterInjectorTest
  fun androidFusedLibraryProject(
    @TestParameter(valuesProvider = AgpVersionProvider::class) agpVersion: AgpVersion,
  ) {
    agpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = androidFusedLibraryProjectSpec(agpVersion)
    // TODO: signing plugin currently unsupported
    val result = project.run(fixtures, testProjectDir, testOptions.copy(signing = TestOptions.Signing.NO_SIGNING))

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("aar").exists()
    // TODO: signing plugin currently unsupported
    // assertThat(result).artifact("aar").isSigned()
    assertThat(result).pom().exists()
    // TODO: signing plugin currently unsupported
    // assertThat(result).pom().isSigned()
    assertThat(result).pom().matchesExpectedPom("aar")
    assertThat(result).module().exists()
    // TODO: signing plugin currently unsupported
    // assertThat(result).module().isSigned()
    assertThat(result).sourcesJar().exists()
    // TODO: signing plugin currently unsupported
    // assertThat(result).sourcesJar().isSigned()
    // TODO: actual sources jar currently unsupported
    // assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    // TODO: signing plugin currently unsupported
    // assertThat(result).javadocJar().isSigned()
  }

  @TestParameterInjectorTest
  fun androidLibraryKotlinProject(
    @TestParameter(valuesProvider = AgpVersionProvider::class) agpVersion: AgpVersion,
    @TestParameter(valuesProvider = KgpVersionProvider::class) kgpVersion: KgpVersion,
  ) {
    agpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)
    kgpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = androidLibraryKotlinProjectSpec(agpVersion, kgpVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("aar").exists()
    assertThat(result).artifact("aar").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    if (agpVersion >= AGP_9_0_0) {
      assertThat(result).pom().matchesExpectedPom("aar", kotlinStdlibCommon(KOTLIN_2_2_10))
    } else {
      assertThat(result).pom().matchesExpectedPom("aar", kotlinStdlibCommon(kgpVersion))
    }
    assertThat(result).module().exists()
    assertThat(result).module().isSigned()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSigned()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSigned()
  }

  @TestParameterInjectorTest
  fun androidLibraryProject(
    @TestParameter(valuesProvider = AgpVersionProvider::class) agpVersion: AgpVersion,
  ) {
    agpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = androidLibraryProjectSpec(agpVersion)
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).artifact("aar").exists()
    assertThat(result).artifact("aar").isSigned()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    if (agpVersion >= AGP_9_0_0) {
      assertThat(result).pom().matchesExpectedPom("aar", kotlinStdlibCommon(KOTLIN_2_2_10))
    } else {
      assertThat(result).pom().matchesExpectedPom("aar")
    }
    assertThat(result).module().exists()
    assertThat(result).module().isSigned()
    assertThat(result).sourcesJar().exists()
    assertThat(result).sourcesJar().isSigned()
    assertThat(result).sourcesJar().containsAllSourceFiles()
    assertThat(result).javadocJar().exists()
    assertThat(result).javadocJar().isSigned()
  }

  @DisabledOnJre(
    value = [JRE.JAVA_25],
    disabledReason = "Dokka 1.x does not support Java 25+.",
  )
  @TestParameterInjectorTest
  fun androidMultiVariantLibraryProject(
    @TestParameter(valuesProvider = AgpVersionProvider::class) agpVersion: AgpVersion,
  ) {
    // regular plugin does not have a way to enable multi variant config
    assume().that(config).isEqualTo(TestOptions.Config.BASE)
    agpVersion.assumeSupportedJdkAndGradleVersion(gradleVersion)

    val project = androidLibraryProjectSpec(agpVersion).copy(
      basePluginConfig = "configure(new AndroidMultiVariantLibrary(true, true))",
    )
    val result = project.run(fixtures, testProjectDir, testOptions)

    assertThat(result).outcome().succeeded()
    assertThat(result).pom().exists()
    assertThat(result).pom().isSigned()
    if (agpVersion >= AGP_9_0_0) {
      assertThat(result).pom().matchesExpectedPom("pom", kotlinStdlibCommon(KOTLIN_2_2_10))
    } else {
      assertThat(result).pom().matchesExpectedPom("pom")
    }
    assertThat(result).module().exists()
    assertThat(result).module().isSigned()

    assertThat(result).artifact("debug", "aar").exists()
    assertThat(result).artifact("debug", "aar").isSigned()
    assertThat(result).sourcesJar("debug").exists()
    assertThat(result).sourcesJar("debug").isSigned()
    assertThat(result).sourcesJar("debug").containsAllSourceFiles()
    assertThat(result).javadocJar("debug").exists()
    assertThat(result).javadocJar("debug").isSigned()

    assertThat(result).artifact("release", "aar").exists()
    assertThat(result).artifact("release", "aar").isSigned()
    assertThat(result).sourcesJar("release").exists()
    assertThat(result).sourcesJar("release").isSigned()
    assertThat(result).sourcesJar("release").containsAllSourceFiles()
    assertThat(result).javadocJar("release").exists()
    assertThat(result).javadocJar("release").isSigned()
  }
}
