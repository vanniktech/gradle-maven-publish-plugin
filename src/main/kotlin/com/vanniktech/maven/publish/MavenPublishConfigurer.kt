package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.MavenPublishPluginExtension.Companion.DEFAULT_TARGET
import com.vanniktech.maven.publish.MavenPublishPluginExtension.Companion.LOCAL_TARGET
import com.vanniktech.maven.publish.tasks.AndroidJavadocs
import com.vanniktech.maven.publish.tasks.AndroidJavadocsJar
import com.vanniktech.maven.publish.tasks.AndroidSourcesJar
import com.vanniktech.maven.publish.tasks.EmptySourcesJar
import com.vanniktech.maven.publish.tasks.GroovydocsJar
import com.vanniktech.maven.publish.tasks.JavadocsJar
import com.vanniktech.maven.publish.tasks.SourcesJar
import org.gradle.api.Project
import org.gradle.api.publish.Publication
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import java.net.URI

@Suppress("TooManyFunctions")
internal class MavenPublishConfigurer(
  private val project: Project,
  private val publishPom: MavenPublishPom,
  private val targets: Iterable<MavenPublishTarget>
) : Configurer {

  private fun configurePom(
    publication: MavenPublication,
    groupId: String = project.group as String, // The plugin initially sets project.group to publishPom.groupId
    artifactId: String = publishPom.artifactId
  ) {
    publication.groupId = groupId
    publication.artifactId = artifactId
    publication.version = project.version as String // The plugin initially sets project.version to publishPom.version

    @Suppress("UnstableApiUsage")
    publication.pom { pom ->

      pom.name.set(publishPom.name)
      pom.description.set(publishPom.description)
      pom.url.set(publishPom.url)
      pom.inceptionYear.set(publishPom.inceptionYear)

      pom.scm {
        it.url.set(publishPom.scmUrl)
        it.connection.set(publishPom.scmConnection)
        it.developerConnection.set(publishPom.scmDeveloperConnection)
      }

      pom.licenses { licenses ->
        licenses.license {
          it.name.set(publishPom.licenseName)
          it.url.set(publishPom.licenseUrl)
          it.distribution.set(publishPom.licenseDistribution)
        }
      }

      pom.developers { developers ->
        developers.developer {
          it.id.set(publishPom.developerId)
          it.name.set(publishPom.developerName)
          it.url.set(publishPom.developerUrl)
        }
      }
    }
  }

  override fun configureTarget(target: MavenPublishTarget) {
    project.publishing.repositories.maven { repo ->
      repo.name = target.repositoryName
      repo.url = target.repositoryUrl(project.version.toString())
      if (target.repositoryUsername != null) {
        repo.credentials {
          it.username = target.repositoryUsername
          it.password = target.repositoryPassword
        }
      }
    }

    // create task that depends on new publishing task for compatibility and easier switching
    project.tasks.register(target.taskName) { task ->
      project.publishing.publications.all { publication ->
        val publishTaskName = publishTaskName(publication, target.repositoryName)
        task.dependsOn(project.tasks.named(publishTaskName))
      }
    }
  }

  private val MavenPublishTarget.repositoryName get(): String {
    return when (name) {
      DEFAULT_TARGET -> "maven"
      LOCAL_TARGET -> "local"
      else -> name
    }
  }

  private fun MavenPublishTarget.repositoryUrl(version: String): URI {
    val url = if (version.endsWith("SNAPSHOT")) {
      snapshotRepositoryUrl ?: releaseRepositoryUrl
    } else {
      releaseRepositoryUrl
    }
    return URI.create(requireNotNull(url))
  }

  private fun publishTaskName(publication: Publication, repository: String) =
    "publish${publication.name.capitalize()}PublicationTo${repository.capitalize()}Repository"

  override fun configureGradlePluginProject() {
    val sourcesJar = project.tasks.register(SOURCES_TASK, SourcesJar::class.java)
    val javadocsJar = project.tasks.register(JAVADOC_TASK, JavadocsJar::class.java)

    project.publishing.publications.withType(MavenPublication::class.java).all {
      if (it.name == "pluginMaven") {
        configurePom(it)
        it.addTaskOutput(javadocsJar)
        it.addTaskOutput(sourcesJar)
      }

      project.extensions.getByType(GradlePluginDevelopmentExtension::class.java).plugins.forEach { plugin ->
        if (it.name == "${plugin.name}PluginMarkerMaven") {
          // keep the current group and artifact ids, they are based on the gradle plugin id
          configurePom(it, groupId = it.groupId, artifactId = it.artifactId)
        }
      }
    }
  }

  override fun configureKotlinMppProject() {
    val javadocsJar = project.tasks.register(JAVADOC_TASK, JavadocsJar::class.java)

    project.publishing.publications.withType(MavenPublication::class.java).all {
      configurePom(it, artifactId = it.artifactId.replace(project.name, publishPom.artifactId))
      it.addTaskOutput(javadocsJar)

      // Source jars are only created for platforms, not the common artifact.
      if (it.name == "kotlinMultiplatform") {
        val emptySourcesJar = project.tasks.register("emptySourcesJar", EmptySourcesJar::class.java)
        it.addTaskOutput(emptySourcesJar)
      }
    }
  }

  override fun configureAndroidArtifacts() {
    val publications = project.publishing.publications
    publications.create(PUBLICATION_NAME, MavenPublication::class.java) { publication ->
      configurePom(publication)
    }

    val publication = project.publishing.publications.getByName(PUBLICATION_NAME) as MavenPublication

    publication.from(project.components.getByName(project.publishExtension.androidVariantToPublish))

    val androidSourcesJar = project.tasks.register("androidSourcesJar", AndroidSourcesJar::class.java)
    publication.addTaskOutput(androidSourcesJar)

    project.tasks.register("androidJavadocs", AndroidJavadocs::class.java)
    val androidJavadocsJar = project.tasks.register("androidJavadocsJar", AndroidJavadocsJar::class.java)
    publication.addTaskOutput(androidJavadocsJar)
  }

  override fun configureJavaArtifacts() {
    val publications = project.publishing.publications
    publications.create(PUBLICATION_NAME, MavenPublication::class.java) { publication ->
      configurePom(publication)
    }

    val publication = project.publishing.publications.getByName(PUBLICATION_NAME) as MavenPublication

    publication.from(project.components.getByName("java"))

    val sourcesJar = project.tasks.register(SOURCES_TASK, SourcesJar::class.java)
    publication.addTaskOutput(sourcesJar)

    val javadocsJar = project.tasks.register(JAVADOC_TASK, JavadocsJar::class.java)
    publication.addTaskOutput(javadocsJar)

    if (project.plugins.hasPlugin("groovy")) {
      val goovydocsJar = project.tasks.register("groovydocJar", GroovydocsJar::class.java)
      publication.addTaskOutput(goovydocsJar)
    }
  }

  private fun MavenPublication.addTaskOutput(taskProvider: TaskProvider<out AbstractArchiveTask>) {
    taskProvider.configure { task ->
      artifact(task)
    }

    targets.forEach { target ->
      val publishTaskName = publishTaskName(this, target.repositoryName)
      project.tasks.named(publishTaskName).configure { publishTask ->
        publishTask.dependsOn(taskProvider)
      }
    }
  }

  companion object {
    const val PUBLICATION_NAME = "maven"
    const val JAVADOC_TASK = "javadocsJar"
    const val SOURCES_TASK = "sourcesJar"
  }
}
