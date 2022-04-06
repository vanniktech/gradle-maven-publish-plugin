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
      // ignore old extension if new extension was already called
      if (sonatypeHost != null && baseExtension.mavenCentral == null) {
        // only print warning when sonatypeHost was not set through a gradle property, we will continue supporting this
        if (extension.sonatypeHostProperty() == null) {
          when(sonatypeHost) {
            SonatypeHost.DEFAULT -> project.logger.warn("The project is currently configured to be published to " +
              "Maven  Central. To maintain the current behavior, you need to explicitly add SONATYPE_HOST=DEFAULT to " +
              "your gradle.properties or add the following to your build files:\n" +
              "mavenPublishing {" +
              "  publishToMavenCentral()" +
              "}")
            SonatypeHost.S01 -> project.logger.warn("Configuring the sonatypeHost through the DSL is deprecated. " +
              "Remove the old option and then add either SONATYPE_HOST=S01 to your gradle.properties or add the " +
              "following to your build files:\n" +
              "mavenPublishing {" +
              "  publishToMavenCentral(\"S01\")" +
              "}")
          }
        }

        baseExtension.publishToMavenCentral(sonatypeHost)
      }

      // ignore old extension if new extension was already called
      if (extension.releaseSigningEnabled && baseExtension.signing == null) {
        if (extension.releaseSigningProperty() == null) {
          project.logger.warn("The project is currently configured to be automatically sign release builds before " +
            "publishing. To maintain the current behavior you will need to explicitly add " +
            "RELEASE_SIGNING_ENABLED=true to your gradle.properties or add the following to your build files:\n" +
            "mavenPublishing {" +
            "  signAllPublications()" +
            "}")
        }
        baseExtension.signAllPublications()
      }

      baseExtension.pomFromGradleProperties()
    }
  }
}
