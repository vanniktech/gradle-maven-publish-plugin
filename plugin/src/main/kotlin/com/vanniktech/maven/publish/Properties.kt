package com.vanniktech.maven.publish

import kotlin.text.toBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import org.gradle.api.Project
import org.gradle.api.provider.Provider

internal fun Project.mavenCentralPublishing(): Boolean {
  val central = providers.gradleProperty("mavenCentralPublishing").orNull
  if (central != null) {
    return central.toBoolean()
  }
  return when (providers.gradleProperty("SONATYPE_HOST").orNull) {
    null -> false
    "CENTRAL_PORTAL" -> true
    else -> error(
      """
      OSSRH was shut down on June 30, 2025. Migrate to CENTRAL_PORTAL instead.
      See more info at https://central.sonatype.org/news/20250326_ossrh_sunset.
      """.trimIndent(),
    )
  }
}

internal fun Project.automaticRelease(): Boolean {
  val automatic = providers.gradleProperty("mavenCentralAutomaticPublishing").orNull
  if (automatic != null) {
    return automatic.toBoolean()
  }
  return providers.gradleProperty("SONATYPE_AUTOMATIC_RELEASE").getOrElse("false").toBoolean()
}

internal fun Project.validateDeployment(): DeploymentValidation {
  val automatic = providers.gradleProperty("mavenCentralDeploymentValidation").orNull
  if (automatic != null) {
    return automatic.toDeploymentValidation()
  }
  return providers.gradleProperty("SONATYPE_DEPLOYMENT_VALIDATION").getOrElse("true").toDeploymentValidation()
}

private fun String.toDeploymentValidation() = when (this) {
  "true" -> DeploymentValidation.PUBLISH
  "false" -> DeploymentValidation.NONE
  else -> DeploymentValidation.valueOf(this)
}

internal fun Project.signAllPublications(): Boolean {
  val sign = providers.gradleProperty("signAllPublications").orNull
  if (sign != null) {
    return sign.toBoolean()
  }
  return providers.gradleProperty("RELEASE_SIGNING_ENABLED").getOrElse("false").toBoolean()
}

internal fun Project.connectTimeout(): Provider<Duration> = providers
  .gradleProperty("SONATYPE_CONNECT_TIMEOUT_SECONDS")
  .map { it.toLong().seconds }
  .orElse(60.seconds)

internal fun Project.closeTimeout(): Provider<Duration> = providers
  .gradleProperty("SONATYPE_CLOSE_TIMEOUT_SECONDS")
  .map { it.toLong().seconds }
  .orElse(60.minutes)

internal fun Project.pollIntervalSeconds(): Provider<Duration> = providers
  .gradleProperty("SONATYPE_POLL_INTERVAL_SECONDS")
  .map { it.toLong().seconds }
  .orElse(5.seconds)
