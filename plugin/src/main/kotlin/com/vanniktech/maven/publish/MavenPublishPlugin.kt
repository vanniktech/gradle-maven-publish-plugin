package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.legacy.configurePlatform
import com.vanniktech.maven.publish.legacy.setCoordinates
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.plugins.signing.SigningPlugin

open class MavenPublishPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    project.plugins.apply(MavenPublishBasePlugin::class.java)
    val baseExtension = project.baseExtension

    // Apply signing immediately. It is also applied by `signAllPublications` but the afterEvaluate means
    // that it's APIs are not available for consumers without also using afterEvaluate.
    project.plugins.apply(SigningPlugin::class.java)

    val extension = project.extensions.create("mavenPublish", MavenPublishPluginExtension::class.java, project)

    project.setCoordinates()
    project.configurePlatform()

    project.afterEvaluate {
      val sonatypeHost = extension.sonatypeHost
      if (sonatypeHost != null) {
        baseExtension.publishToMavenCentral(sonatypeHost)
      }

      if (extension.releaseSigningEnabled) {
        baseExtension.signAllPublications()
      }

      baseExtension.pomFromGradleProperties()
    }
  }
}
