package com.vanniktech.maven.publish

import com.google.common.truth.Truth.assertThat
import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import com.vanniktech.maven.publish.util.GradleVersion
import com.vanniktech.maven.publish.util.GradleVersionProvider
import com.vanniktech.maven.publish.util.assumeSupportedJdkVersion
import java.nio.file.Path
import kotlin.io.path.writeText
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.io.TempDir

class MavenCentralCredentialsTest {
  @TempDir
  lateinit var testProjectDir: Path

  @TestParameterInjectorTest
  fun missingCredentialsFailWithHelpfulMessage(
    @TestParameter(valuesProvider = GradleVersionProvider::class) gradleVersion: GradleVersion,
  ) {
    gradleVersion.assumeSupportedJdkVersion()

    val result = runner(gradleVersion).buildAndFail()

    assertThat(result.output).contains("mavenCentralUsername not found")
  }

  @TestParameterInjectorTest
  fun credentialsFromGradleProperties(
    @TestParameter(valuesProvider = GradleVersionProvider::class) gradleVersion: GradleVersion,
  ) {
    gradleVersion.assumeSupportedJdkVersion()

    val result = runner(gradleVersion, gradleProperties = CREDENTIALS).build()

    assertThat(result.task(TASK)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
  }

  @TestParameterInjectorTest
  fun credentialsFromCommandLineProperty(
    @TestParameter(valuesProvider = GradleVersionProvider::class) gradleVersion: GradleVersion,
  ) {
    gradleVersion.assumeSupportedJdkVersion()

    val result = runner(gradleVersion, commandLineProperties = CREDENTIALS).build()

    assertThat(result.task(TASK)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
  }

  @TestParameterInjectorTest
  fun credentialsFromEnvironmentVariable(
    @TestParameter(valuesProvider = GradleVersionProvider::class) gradleVersion: GradleVersion,
  ) {
    gradleVersion.assumeSupportedJdkVersion()

    val environment = CREDENTIALS.mapKeys { "ORG_GRADLE_PROJECT_${it.key}" }
    val result = runner(gradleVersion, environmentVariables = environment).build()

    assertThat(result.task(TASK)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
  }

  @TestParameterInjectorTest
  fun credentialsFromProjectExtraProperties(
    @TestParameter(valuesProvider = GradleVersionProvider::class) gradleVersion: GradleVersion,
  ) {
    gradleVersion.assumeSupportedJdkVersion()

    val result = runner(gradleVersion, extraProperties = CREDENTIALS).build()

    assertThat(result.task(TASK)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
  }

  @TestParameterInjectorTest
  fun credentialsResolveWhenSetInBothGradlePropertyAndExtra(
    @TestParameter(valuesProvider = GradleVersionProvider::class) gradleVersion: GradleVersion,
  ) {
    gradleVersion.assumeSupportedJdkVersion()

    val result = runner(gradleVersion, gradleProperties = CREDENTIALS, extraProperties = CREDENTIALS).build()

    assertThat(result.task(TASK)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
  }

  private fun runner(
    gradleVersion: GradleVersion,
    gradleProperties: Map<String, String> = emptyMap(),
    commandLineProperties: Map<String, String> = emptyMap(),
    environmentVariables: Map<String, String> = emptyMap(),
    extraProperties: Map<String, String> = emptyMap(),
  ): GradleRunner {
    testProjectDir.resolve("settings.gradle").writeText(
      """
      pluginManagement {
        repositories {
          mavenLocal()
          mavenCentral()
          gradlePluginPortal()
        }
      }

      rootProject.name = "test-project"
      """.trimIndent(),
    )

    val extraBlock = extraProperties.entries.joinToString(separator = "\n") { "ext[\"${it.key}\"] = \"${it.value}\"" }
    testProjectDir.resolve("build.gradle").writeText(
      """
      plugins {
        id "com.vanniktech.maven.publish.base" version "${IntegrationTestBuildConfig.VERSION_NAME}"
      }

      group = "com.example"
      version = "1.0.0-SNAPSHOT"

      $extraBlock

      mavenPublishing {
        publishToMavenCentral()
      }
      """.trimIndent(),
    )

    testProjectDir.resolve("gradle.properties").writeText(
      buildString {
        appendLine("org.gradle.vfs.watch=false")
        gradleProperties.forEach { appendLine("${it.key}=${it.value}") }
      },
    )

    val arguments = mutableListOf(TASK, "--stacktrace", "--configuration-cache")
    commandLineProperties.forEach { arguments += "-P${it.key}=${it.value}" }

    val environment = System.getenv().filterKeys { it !in CREDENTIAL_ENVIRONMENT_VARIABLES } + environmentVariables

    return GradleRunner
      .create()
      .withGradleVersion(gradleVersion.value)
      .withProjectDir(testProjectDir.toFile())
      .withDebug(false)
      .withEnvironment(environment)
      .withArguments(arguments)
  }

  private companion object {
    const val TASK = ":prepareMavenCentralPublishing"

    val CREDENTIALS = mapOf(
      "mavenCentralUsername" to "username",
      "mavenCentralPassword" to "password",
    )

    val CREDENTIAL_ENVIRONMENT_VARIABLES = CREDENTIALS.keys.map { "ORG_GRADLE_PROJECT_$it" }.toSet()
  }
}
