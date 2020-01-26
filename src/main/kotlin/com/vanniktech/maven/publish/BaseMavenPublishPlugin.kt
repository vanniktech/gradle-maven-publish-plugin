package com.vanniktech.maven.publish

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.util.VersionNumber

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

    configureJavadoc(p)

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
    }
  }

  private fun configureJavadoc(project: Project) {
    project.tasks.withType(Javadoc::class.java) {
      val options = it.options as StandardJavadocDocletOptions
      if(JavaVersion.current().isJava9Compatible) {
        options.addBooleanOption("html5", true)
      }
      if (JavaVersion.current().isJava8Compatible) {
        options.addStringOption("Xdoclint:none", "-quiet")
      }
    }
  }

  protected abstract fun setupConfigurerForAndroid(project: Project, configurer: Configurer)

  protected abstract fun setupConfigurerForJava(project: Project, configurer: Configurer)

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
