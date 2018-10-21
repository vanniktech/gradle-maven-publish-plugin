package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.MavenPublishPluginExtension.Companion.DEFAULT_TARGET
import com.vanniktech.maven.publish.MavenPublishPluginExtension.Companion.LOCAL_TARGET
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency.ARCHIVES_CONFIGURATION
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.plugins.MavenPlugin
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningPlugin

internal class UploadArchivesConfigurer(
  private val project: Project,
  private val targets: Iterable<MavenPublishTarget>,
  private val configureMavenDeployer: Upload.(Project, MavenPublishTarget) -> Unit
) : Configurer {

  init {
    project.plugins.apply(MavenPlugin::class.java)
    project.plugins.apply(SigningPlugin::class.java)

    project.signing.apply {
      setRequired(project.isSigningRequired)
      sign(project.configurations.getByName(ARCHIVES_CONFIGURATION))
    }
    project.tasks.withType(Sign::class.java).all { sign ->
      sign.onlyIf { _ ->
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
    upload.configureMavenDeployer(project, target)
  }

  private fun getUploadTask(name: String, taskName: String): Upload =
    when (name) {
      DEFAULT_TARGET -> project.tasks.getByName(taskName) as Upload
      LOCAL_TARGET -> createUploadTask(taskName, "Installs the artifacts to the local Maven repository.")
      else -> createUploadTask(taskName, "Upload all artifacts to $name")
    }

  private fun createUploadTask(name: String, description: String): Upload =
    project.tasks.create(name, Upload::class.java) {
      it.group = "upload"
      it.description = description
      it.configuration = project.configurations.getByName(ARCHIVES_CONFIGURATION)
    }

  override fun addComponent(component: SoftwareComponent) = Unit

  override fun addTaskOutput(task: AbstractArchiveTask) {
    project.artifacts.add(ARCHIVES_CONFIGURATION, task)
  }
}
