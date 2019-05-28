package com.vanniktech.maven.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Upload
import org.gradle.util.VersionNumber

internal abstract class BaseMavenPublishPlugin : Plugin<Project> {

  override fun apply(p: Project) {
    val extension = p.extensions.create("mavenPublish", MavenPublishPluginExtension::class.java, p)

    val gradleVersion = VersionNumber.parse(p.gradle.gradleVersion)
    if (gradleVersion < VersionNumber(4, 10, 1, null)) {
      throw IllegalArgumentException("You need gradle version 4.10.1 or higher")
    }

    val pom = MavenPublishPom.fromProject(p)
    p.group = requireNotNull(pom.groupId) { "groupId is required to be set" }
    p.version = requireNotNull(pom.version) { "version is required to be set" }

    p.afterEvaluate { project ->
      val configurer = when {
        extension.useMavenPublish -> MavenPublishConfigurer(project)
        else -> UploadArchivesConfigurer(project, extension.targets, ::configureMavenDeployer)
      }

      extension.targets.all {
        checkNotNull(it.releaseRepositoryUrl) {
          "releaseRepositoryUrl of ${it.name} is required to be set"
        }
        configurer.configureTarget(it)
      }

      if (project.plugins.hasPlugin("com.android.library")) {
        setupConfigurerForAndroid(project, configurer)
      } else {
        setupConfigurerForJava(project, configurer)
      }

      java8Javadoc(project)
    }
  }

  protected abstract fun setupConfigurerForAndroid(project: Project, configurer: Configurer)

  protected abstract fun setupConfigurerForJava(project: Project, configurer: Configurer)

  protected abstract fun java8Javadoc(project: Project)

  protected abstract fun configureMavenDeployer(
    upload: Upload,
    project: Project,
    target: MavenPublishTarget
  )
}
