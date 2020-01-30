package com.vanniktech.maven.publish

import org.gradle.api.JavaVersion
import com.vanniktech.maven.publish.nexus.NexusConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
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

    configureJavadoc(p)
    configureDokka(p)

    p.afterEvaluate { project ->
      val configurer = when {
        extension.useLegacyMode -> UploadArchivesConfigurer(project, ::configureMavenDeployer)
        else -> MavenPublishConfigurer(project)
      }

      extension.targets.all {
        checkNotNull(it.releaseRepositoryUrl) {
          "releaseRepositoryUrl of ${it.name} is required to be set"
        }
        configurer.configureTarget(it)
      }

      if (project.plugins.hasPlugin("com.android.library")) {
        configurer.configureAndroidArtifacts()
      } else {
        configurer.configureJavaArtifacts()
      }

      NexusConfigurer(project)
    }
  }

  private fun configureJavadoc(project: Project) {
    project.tasks.withType(Javadoc::class.java).configureEach {
      val options = it.options as StandardJavadocDocletOptions
      if (JavaVersion.current().isJava9Compatible) {
        options.addBooleanOption("html5", true)
      }
      if (JavaVersion.current().isJava8Compatible) {
        options.addStringOption("Xdoclint:none", "-quiet")
      }
    }
  }

  private fun configureDokka(project: Project) {
    project.plugins.withId("org.jetbrains.kotlin.jvm") {
      project.plugins.apply(PLUGIN_DOKKA)
    }
    project.plugins.withId("org.jetbrains.kotlin.android") {
      project.plugins.apply(PLUGIN_DOKKA)
    }
    project.plugins.withId(PLUGIN_DOKKA) {
      project.tasks.withType(DokkaTask::class.java).configureEach {
        if (it.outputDirectory.isEmpty()) {
          val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
          it.outputDirectory = javaConvention.docsDir.resolve("dokka").toRelativeString(project.projectDir)
        }
      }
    }
  }

  protected abstract fun configureMavenDeployer(
    upload: Upload,
    project: Project,
    target: MavenPublishTarget
  )

  companion object {
    const val MINIMUM_GRADLE_MAJOR = 4
    const val MINIMUM_GRADLE_MINOR = 10
    const val MINIMUM_GRADLE_MICRO = 1

    const val PLUGIN_DOKKA = "org.jetbrains.dokka"
  }
}
