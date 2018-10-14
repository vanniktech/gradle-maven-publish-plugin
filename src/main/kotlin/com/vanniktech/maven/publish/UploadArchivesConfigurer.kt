package com.vanniktech.maven.publish

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency.ARCHIVES_CONFIGURATION
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.plugins.MavenPlugin
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import java.util.concurrent.Callable
import kotlin.math.sign

internal abstract class UploadArchivesConfigurer(
  protected val project: Project,
  private val extension: MavenPublishPluginExtension
) : Configurer {

  init {
    project.plugins.apply(MavenPlugin::class.java)
    project.plugins.apply(SigningPlugin::class.java)

    project.signing.apply {
      setRequired(Callable<Boolean> { !project.version.toString().contains("SNAPSHOT") })
      sign(project.configurations.getByName(ARCHIVES_CONFIGURATION))
    }
    project.tasks.withType(Sign::class.java).all { sign ->
      sign.onlyIf { _ ->
        val signedTargets = extension.targets.filter { it.signing }
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

  override fun addComponent(component: SoftwareComponent) = Unit

  override fun addTaskOutput(task: AbstractArchiveTask) {
    project.artifacts.add(ARCHIVES_CONFIGURATION, task)
  }

  private val Project.signing get() = extensions.getByType(SigningExtension::class.java)
}
