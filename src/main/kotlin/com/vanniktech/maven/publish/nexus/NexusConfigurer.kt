package com.vanniktech.maven.publish.nexus

import com.vanniktech.maven.publish.MavenPublishPluginExtension
import org.gradle.api.Project

class NexusConfigurer(project: Project) {
  init {
    val mavenPublishPluginExtension = project.extensions.getByType(MavenPublishPluginExtension::class.java)

    // create on rootProject so that it only exists once
    // there is no maybeRegister https://github.com/gradle/gradle/issues/6243
    val task = project.rootProject.tasks.maybeCreate("closeAndReleaseRepository", CloseAndReleaseRepositoryTask::class.java)
    task.description = "Closes and releases an artifacts repository in Nexus"
    task.group = "release"
    task.nexusOptions = mavenPublishPluginExtension.nexusOptions
  }
}
