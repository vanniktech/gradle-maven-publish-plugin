package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.MavenPublishPluginExtension.Companion.DEFAULT_TARGET
import com.vanniktech.maven.publish.MavenPublishPluginExtension.Companion.LOCAL_TARGET
import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin as GradleMavenPublishPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.plugins.signing.SigningPlugin
import java.net.URI

internal class MavenPublishConfigurer(private val project: Project) : Configurer {

  private val publication: MavenPublication

  private val publishTaskProviders = mutableListOf<TaskProvider<*>>()

  init {
    project.plugins.apply(GradleMavenPublishPlugin::class.java)
    project.plugins.apply(SigningPlugin::class.java)

    val publications = project.publishing.publications
    publication = publications.create("maven", MavenPublication::class.java) { publication ->
      val publishPom = MavenPublishPom.fromProject(project)

      publication.groupId = publishPom.groupId
      publication.artifactId = publishPom.artifactId
      publication.version = publishPom.version

      publication.pom { pom ->
        pom.name.set(publishPom.name)
        pom.packaging = publishPom.packaging
        pom.description.set(publishPom.description)
        pom.url.set(publishPom.url)

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

    project.signing.apply {
      setRequired(project.isSigningRequired)
      sign(publication)
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

    val publishTaskName = publishTaskName(target.repositoryName)
    publishTaskProviders.add(project.tasks.named(publishTaskName))

    // create task that depends on new publishing task for compatibility and easier switching
    project.tasks.register(target.taskName) {
      it.dependsOn(project.tasks.named(publishTaskName))
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

  private fun publishTaskName(repository: String) =
    "publish${publication.name.capitalize()}PublicationTo${repository.capitalize()}Repository"

  override fun addComponent(component: SoftwareComponent) {
    publication.from(component)
  }

  override fun addTaskOutput(taskProvider: TaskProvider<AbstractArchiveTask>) {
    taskProvider.configure { task ->
      publication.artifact(task)
    }
    publishTaskProviders.forEach {
      it.configure { publishTask ->
        publishTask.dependsOn(taskProvider)
      }
    }
  }
}
