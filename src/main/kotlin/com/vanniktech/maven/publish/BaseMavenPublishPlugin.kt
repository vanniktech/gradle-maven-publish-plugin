package com.vanniktech.maven.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Upload
import org.gradle.util.VersionNumber
import org.jetbrains.dokka.gradle.DokkaTask

internal abstract class BaseMavenPublishPlugin : Plugin<Project> {

  override fun apply(p: Project) {
    val extension = p.extensions.create("mavenPublish", MavenPublishPluginExtension::class.java, p)

    val gradleVersion = VersionNumber.parse(p.gradle.gradleVersion)
    if (gradleVersion < VersionNumber(MINIMUM_GRADLE_MAJOR, MINIMUM_GRADLE_MINOR, MINIMUM_GRADLE_MICRO, null)) {
      throw IllegalArgumentException("You need gradle version 4.10.1 or higher")
    }

    val pom = MavenPublishPom.fromProject(p)
    p.group = pom.groupId
    p.version = pom.version

    configureDokka(p)

    p.afterEvaluate { project ->
      val configurer = when {
        extension.useLegacyMode -> UploadArchivesConfigurer(project, extension.targets, ::configureMavenDeployer)
        else -> MavenPublishConfigurer(project)
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

  private fun configureDokka(project: Project) {
    project.plugins.withId("org.jetbrains.kotlin.jvm") {
      project.plugins.apply("org.jetbrains.dokka")
    }
    project.plugins.withId("org.jetbrains.kotlin.android") {
      project.plugins.apply("org.jetbrains.dokka")
    }
    project.plugins.withId("org.jetbrains.dokka") {
      project.tasks.withType(DokkaTask::class.java).configureEach {
        if (it.outputDirectory.isEmpty()) {
          val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
          it.outputDirectory = javaConvention.docsDir.resolve("dokka").toRelativeString(project.projectDir)
        }
      }
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

  companion object {
    const val MINIMUM_GRADLE_MAJOR = 4
    const val MINIMUM_GRADLE_MINOR = 10
    const val MINIMUM_GRADLE_MICRO = 1
  }
}
