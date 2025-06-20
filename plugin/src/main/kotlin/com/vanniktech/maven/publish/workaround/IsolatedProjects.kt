package com.vanniktech.maven.publish.workaround

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.util.GradleVersion

/**
 * Use isolated project compatible methods of accessing the root project
 * if they are available.
 *
 * Can be removed when Gradle 8.13 is the minimum supported version.
 */
internal fun Project.rootProjectBuildDir(): Provider<Directory> {
  // TODO: remove this when Gradle 8.8/8.13 is the minimum supported version.
  return when {
    GradleVersion.current() >= SETTINGS_DIRECTORY_GRADLE_VERSION -> project.provider {
      layout.settingsDirectory.dir("build")
    }
    GradleVersion.current() >= ISOLATED_PROJECT_VIEW_GRADLE_VERSION -> project.provider {
      isolated.rootProject.projectDirectory.dir("build")
    }
    else -> {
      @Suppress("GradleProjectIsolation")
      rootProject.layout.buildDirectory
    }
  }
}

private val ISOLATED_PROJECT_VIEW_GRADLE_VERSION = GradleVersion.version("8.8-rc-1")
private val SETTINGS_DIRECTORY_GRADLE_VERSION = GradleVersion.version("8.13-rc-1")
