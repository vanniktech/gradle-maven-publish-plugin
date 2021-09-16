package com.vanniktech.maven.publish

import org.gradle.api.Plugin
import org.gradle.api.Project

open class MavenPublishRootPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    if (project != project.rootProject) {
      throw IllegalArgumentException("MavenPublishRootPlugin should be applied to root project, not ${project.path}")
    }

    project.extensions.create("mavenPublishingRoot", MavenPublishRootExtension::class.java, project)
  }
}
