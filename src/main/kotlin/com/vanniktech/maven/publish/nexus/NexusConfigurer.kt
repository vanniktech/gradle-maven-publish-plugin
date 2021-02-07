package com.vanniktech.maven.publish.nexus

import com.vanniktech.maven.publish.MavenPublishPluginExtension
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException

internal class NexusConfigurer(project: Project) {
  init {
    val mavenPublishPluginExtension = project.extensions.getByType(MavenPublishPluginExtension::class.java)

    @Suppress("SwallowedException")
    try {
      project.rootProject.tasks.named("closeAndReleaseRepository")
    } catch (e: UnknownTaskException) {
      project.rootProject.tasks.register("closeAndReleaseRepository", CloseAndReleaseRepositoryTask::class.java) {
        it.description = "Closes and releases an artifacts repository in Nexus"
        it.group = "release"
        it.nexusOptions = mavenPublishPluginExtension.nexusOptions
      }
    }
  }
}
