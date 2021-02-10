@file:Suppress("DEPRECATION")
package com.vanniktech.maven.publish.legacy

import com.vanniktech.maven.publish.MavenPublishPluginExtension
import com.vanniktech.maven.publish.MavenPublishPluginExtension.Companion.DEFAULT_TARGET
import com.vanniktech.maven.publish.MavenPublishPluginExtension.Companion.LOCAL_TARGET
import com.vanniktech.maven.publish.MavenPublishTarget
import com.vanniktech.maven.publish.findOptionalProperty
import com.vanniktech.maven.publish.publishing
import org.gradle.api.Project

@Suppress("ComplexMethod")
internal fun Project.configureTargets(extension: MavenPublishPluginExtension) {
  if (findOptionalProperty("RELEASE_REPOSITORY_URL") != null) {
    logger.warn("Modifying the default repository by setting the RELEASE_REPOSITORY_URL property " +
      "is deprecated. Use the default Gradle APIs to configure additional targets/repositories " +
      "https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories")
  }
  if (findOptionalProperty("SNAPSHOT_REPOSITORY_URL") != null) {
    logger.warn("Modifying the default repository by setting the SNAPSHOT_REPOSITORY_URL env var " +
      "is deprecated. Use the default Gradle APIs to configure additional targets/repositories " +
      "https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories")
  }
  if (System.getenv("RELEASE_REPOSITORY_URL") != null) {
    logger.warn("Modifying the default repository by setting the RELEASE_REPOSITORY_URL property " +
      "is deprecated. Use the default Gradle APIs to configure additional targets/repositories " +
      "https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories")
  }
  if (System.getenv("SNAPSHOT_REPOSITORY_URL") != null) {
    logger.warn("Modifying the default repository by setting the SNAPSHOT_REPOSITORY_URL env var " +
      "is deprecated. Use the default Gradle APIs to configure additional targets/repositories " +
      "https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories")
  }

  if (findOptionalProperty("SONATYPE_NEXUS_USERNAME") != null) {
    logger.warn("The property SONATYPE_NEXUS_USERNAME is deprecated. Use mavenCentralRepositoryUsername instead.")
  }
  if (System.getenv("SONATYPE_NEXUS_USERNAME") != null) {
    logger.warn("The env var SONATYPE_NEXUS_USERNAME is deprecated. Use the Gradle property " +
      "mavenCentralRepositoryUsername or the env var ORG_GRADLE_PROJECT_mavenCentralRepositoryUsername instead.")
  }
  if (findOptionalProperty("SONATYPE_NEXUS_PASSWORD") != null) {
    logger.warn("The property SONATYPE_NEXUS_PASSWORD is deprecated. Use mavenCentralRepositoryPassword instead.")
  }
  if (System.getenv("SONATYPE_NEXUS_PASSWORD") != null) {
    logger.warn("The env var SONATYPE_NEXUS_PASSWORD is deprecated. Use the Gradle property " +
      "mavenCentralRepositoryPassword or the env var ORG_GRADLE_PROJECT_mavenCentralRepositoryPassword instead.")
  }

  afterEvaluate {
    extension.targets.all {
      if (it.name == DEFAULT_TARGET && it != extension.defaultTarget) {
        logger.warn("Modifying the default ${it.name} target is deprecated. Use " +
          "the default Gradle APIs to configure it as an additional target/repository " +
          "https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories")
      } else if (it.name == LOCAL_TARGET && it != extension.localTarget) {
        logger.warn("Modifying the default ${it.name} target is deprecated. Use " +
          "the default Gradle APIs to configure it as an additional target/repository " +
          "https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories")
      } else if (it.name != DEFAULT_TARGET && it.name != LOCAL_TARGET){
        logger.warn("Adding additional targets through this API is deprecated. Use " +
          "the default Gradle APIs to add additional targets/repositories " +
          "https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories")
      }

      checkNotNull(it.releaseRepositoryUrl) {
        "releaseRepositoryUrl of ${it.name} is required to be set"
      }
      configureTarget(it)
    }
  }
}

private fun Project.configureTarget(target: MavenPublishTarget) {
  publishing.repositories.maven { repo ->
    repo.name = target.repositoryName
    repo.setUrl(target.repositoryUrl(version.toString()))
    if (target.repositoryUsername != null) {
      repo.credentials {
        it.username = target.repositoryUsername
        it.password = target.repositoryPassword
      }
    }
  }

  // create task that depends on new publishing task for compatibility and easier switching
  tasks.register(target.taskName) { task ->
    publishing.publications.all { publication ->
      val publishTaskName = "publish${publication.name.capitalize()}Publication" +
        "To${target.repositoryName.capitalize()}Repository"
      task.dependsOn(tasks.named(publishTaskName))
    }

    task.doLast {
      if (task.name == LOCAL_TARGET) {
        logger.warn("The task ${task.name} is deprecated use publishToMavenLocal instead.")
      } else {
        logger.warn("The task ${task.name} is deprecated use publish instead.")
      }
    }
  }
}

private val MavenPublishTarget.repositoryName get(): String {
  return when (name) {
    DEFAULT_TARGET -> "maven"
    LOCAL_TARGET -> "local"
    else -> name
  }
}

@Suppress("UnsafeCallOnNullableType")
private fun MavenPublishTarget.repositoryUrl(version: String): String {
  return if (version.endsWith("SNAPSHOT")) {
    snapshotRepositoryUrl ?: releaseRepositoryUrl!!
  } else {
    releaseRepositoryUrl!!
  }
}
