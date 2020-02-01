package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.MavenPublishPluginExtension.Companion.DEFAULT_TARGET
import com.vanniktech.maven.publish.MavenPublishPluginExtension.Companion.LOCAL_TARGET
import com.vanniktech.maven.publish.tasks.AndroidJavadocs
import com.vanniktech.maven.publish.tasks.AndroidJavadocsJar
import com.vanniktech.maven.publish.tasks.AndroidSourcesJar
import com.vanniktech.maven.publish.tasks.GroovydocsJar
import com.vanniktech.maven.publish.tasks.JavadocsJar
import com.vanniktech.maven.publish.tasks.SourcesJar
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency.ARCHIVES_CONFIGURATION
import org.gradle.api.plugins.MavenPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.plugins.signing.SigningPlugin

internal class UploadArchivesConfigurer(
  private val project: Project,
  private val configureMavenDeployer: Upload.(Project, MavenPublishTarget) -> Unit
) : Configurer {

  private val uploadTaskProviders = mutableListOf<TaskProvider<Upload>>()

  init {
    project.plugins.apply(MavenPlugin::class.java)

    if (project.isSigningRequired.call() && project.project.publishExtension.releaseSigningEnabled) {
      @Suppress("UnstableApiUsage")
      project.signing.sign(project.configurations.getByName(ARCHIVES_CONFIGURATION))
    }
  }

  override fun configureTarget(target: MavenPublishTarget) {
    val upload = getUploadTask(target.name, target.taskName)
    upload.configure {
      it.configureMavenDeployer(project, target)
    }
    uploadTaskProviders.add(upload)
  }

  @Suppress("UNCHECKED_CAST")
  private fun getUploadTask(name: String, taskName: String): TaskProvider<Upload> =
    when (name) {
      DEFAULT_TARGET -> project.tasks.named(taskName) as TaskProvider<Upload>
      LOCAL_TARGET -> createUploadTask(taskName, "Installs the artifacts to the local Maven repository.")
      else -> createUploadTask(taskName, "Upload all artifacts to $name")
    }

  private fun createUploadTask(name: String, description: String): TaskProvider<Upload> =
    project.tasks.register(name, Upload::class.java) {
      it.group = "upload"
      it.description = description
      it.configuration = project.configurations.getByName(ARCHIVES_CONFIGURATION)
    }

  override fun configureAndroidArtifacts() {
    val androidSourcesJar = project.tasks.register("androidSourcesJar", AndroidSourcesJar::class.java)
    addTaskOutput(androidSourcesJar)

    project.tasks.register("androidJavadocs", AndroidJavadocs::class.java)
    val androidJavadocsJar = project.tasks.register("androidJavadocsJar", AndroidJavadocsJar::class.java)
    addTaskOutput(androidJavadocsJar)
  }

  override fun configureJavaArtifacts() {
    val sourcesJar = project.tasks.register("sourcesJar", SourcesJar::class.java)
    addTaskOutput(sourcesJar)

    val javadocsJar = project.tasks.register("javadocsJar", JavadocsJar::class.java)
    addTaskOutput(javadocsJar)

    if (project.plugins.hasPlugin("groovy")) {
      val goovydocsJar = project.tasks.register("groovydocJar", GroovydocsJar::class.java)
      addTaskOutput(goovydocsJar)
    }
  }

  private fun addTaskOutput(taskProvider: TaskProvider<out AbstractArchiveTask>) {
    taskProvider.configure { task ->
        project.artifacts.add(ARCHIVES_CONFIGURATION, task)
    }
    uploadTaskProviders.forEach {
      it.configure { uploadTask ->
        uploadTask.dependsOn(taskProvider)
      }
    }
  }
}
