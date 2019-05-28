package com.vanniktech.maven.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Upload

internal abstract class BaseMavenPublishPlugin : Plugin<Project> {

  override fun apply(p: Project) {
    val extension = p.extensions.create("mavenPublish", MavenPublishPluginExtension::class.java, p)

    checkGradleVersion(p)

    val pom = MavenPublishPom.fromProject(p)
    p.group = requireNotNull(pom.groupId) { "groupId is required to be set" }
    p.version = requireNotNull(pom.version) { "version is required to be set" }

    p.afterEvaluate { project ->
      val configurer = when {
        extension.useMavenPublish -> MavenPublishConfigurer(project)
        else -> UploadArchivesConfigurer(project, extension.targets, ::configureMavenDeployer)
      }

      extension.targets.all {
        println("configuring target ${it.name}")
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

  private fun checkGradleVersion(p: Project) {
    val gradleVersion = p.gradle.gradleVersion
    val v = gradleVersion.split(".")
    when {
      v.size < 2 -> throw IllegalArgumentException("Unrecognized gradle version: $gradleVersion")
      v[0].toInt() < 4 -> throw IllegalArgumentException("You need gradle version 4.10.1 or higher")
      v[0].toInt() == 4 && v[1].toInt() < 10 -> throw IllegalArgumentException("You need gradle version 4.10.1 or higher")
      v.size == 2 && v[0].toInt() == 4 && v[1].toInt() == 10 -> throw IllegalArgumentException("You need gradle version 4.10.1 or higher")
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
