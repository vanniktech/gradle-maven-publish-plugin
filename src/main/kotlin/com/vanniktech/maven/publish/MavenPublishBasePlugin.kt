package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.nexus.CloseAndReleaseRepositoryTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.util.VersionNumber
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin as GradleMavenPublishPlugin

open class MavenPublishBasePlugin : Plugin<Project> {

  override fun apply(project: Project) {
    val gradleVersion = VersionNumber.parse(project.gradle.gradleVersion)
    if (gradleVersion < MIN_GRADLE_VERSION) {
      error("You need Gradle version 6.6.0 or higher")
    }

    project.plugins.apply(GradleMavenPublishPlugin::class.java)

    project.extensions.create("mavenPublishing", MavenPublishBaseExtension::class.java, project)

    maybeRegisterCloseAndReleaseRepository(project)
  }

  private fun maybeRegisterCloseAndReleaseRepository(project: Project) {
    @Suppress("SwallowedException")
    try {
      project.rootProject.tasks.named("closeAndReleaseRepository")
    } catch (e: UnknownTaskException) {
      project.rootProject.tasks.register("closeAndReleaseRepository", CloseAndReleaseRepositoryTask::class.java) {
        it.description = "Closes and releases an artifacts repository in Nexus"
        it.group = "release"
      }
    }
  }

  private companion object {
    val MIN_GRADLE_VERSION = VersionNumber.parse("6.6.0")
  }
}
