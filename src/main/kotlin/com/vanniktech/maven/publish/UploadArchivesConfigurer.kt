package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.MavenPublishPluginExtension.Companion.DEFAULT_TARGET
import com.vanniktech.maven.publish.MavenPublishPluginExtension.Companion.LOCAL_TARGET
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency.ARCHIVES_CONFIGURATION
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.plugins.MavenPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningPlugin

internal class UploadArchivesConfigurer(
  private val project: Project,
  private val targets: Iterable<MavenPublishTarget>,
  private val configureMavenDeployer: Upload.(Project, MavenPublishTarget) -> Unit
) : Configurer {

  private val uploadTaskProviders = mutableListOf<TaskProvider<Upload>>()

  init {
    project.plugins.apply(MavenPlugin::class.java)
    project.plugins.apply(SigningPlugin::class.java)

    project.signing.apply {
      setRequired(project.isSigningRequired)
      sign(project.configurations.getByName(ARCHIVES_CONFIGURATION))
    }
    project.tasks.withType(Sign::class.java).all { sign ->
      sign.onlyIf {
        val signedTargets = targets.filter { it.signing }
        sign.logger.info("Targets that should be signed: ${signedTargets.map { it.name }}")
        signedTargets.any { target ->
          val task = project.tasks.getByName(target.taskName)
          project.gradle.taskGraph.hasTask(task).also {
            sign.logger.info("Task for ${target.name} will be executed: $it")
          }
        }
      }
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

  override fun addComponent(component: SoftwareComponent) = Unit

  override fun addTaskOutput(taskProvider: TaskProvider<AbstractArchiveTask>) {
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
