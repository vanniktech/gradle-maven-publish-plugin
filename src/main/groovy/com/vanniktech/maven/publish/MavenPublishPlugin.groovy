package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.tasks.AndroidSourcesJar
import com.vanniktech.maven.publish.tasks.AndroidJavadocs
import com.vanniktech.maven.publish.tasks.AndroidJavadocsJar
import com.vanniktech.maven.publish.tasks.GroovydocsJar
import com.vanniktech.maven.publish.tasks.SourcesJar
import com.vanniktech.maven.publish.tasks.JavadocsJar
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.artifacts.maven.MavenPom
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.Upload
import org.jetbrains.annotations.NotNull

class MavenPublishPlugin extends BaseMavenPublishPlugin {

  @Override
  protected void setupConfigurerForAndroid(@NotNull Project project, @NotNull Configurer configurer) {
    MavenPublishPluginExtension extension = project.extensions.getByType(MavenPublishPluginExtension.class)

    if (!extension.useLegacyMode) {
      configurer.addComponent(project.components.getByName(extension.androidVariantToPublish))
    }

    def androidSourcesJar = project.tasks.register("androidSourcesJar", AndroidSourcesJar.class)
    configurer.addTaskOutput(androidSourcesJar)

    project.tasks.register("androidJavadocs", AndroidJavadocs.class)
    def androidJavadocsJar = project.tasks.register("androidJavadocsJar", AndroidJavadocsJar.class)
    configurer.addTaskOutput(androidJavadocsJar)
  }

  @Override
  protected void setupConfigurerForJava(@NotNull Project project, @NotNull Configurer configurer) {
    PluginContainer plugins = project.plugins

    configurer.addComponent(project.components.java)

    def sourcesJar = project.tasks.register("sourcesJar", SourcesJar.class)
    configurer.addTaskOutput(sourcesJar)

    def javadocsJar = project.tasks.register("javadocsJar", JavadocsJar.class)
    configurer.addTaskOutput(javadocsJar)

    if (plugins.hasPlugin('groovy')) {
      def goovydocsJar = project.tasks.register("groovydocJar", GroovydocsJar.class)
      configurer.addTaskOutput(goovydocsJar)
    }
  }

  @Override
  protected void configureMavenDeployer(Upload upload, Project project, MavenPublishTarget target) {
    upload.repositories {
      mavenDeployer {
        if (target.signing) {
          beforeDeployment { MavenDeployment deployment -> project.signing.signPom(deployment) }
        }

        repository(url: target.releaseRepositoryUrl) {
          authentication(userName: target.repositoryUsername, password: target.repositoryPassword)
        }

        if (target.snapshotRepositoryUrl != null) {
          snapshotRepository(url: target.snapshotRepositoryUrl) {
            authentication(userName: target.repositoryUsername, password: target.repositoryPassword)
          }
        }

        configurePom(project, pom)
      }
    }
  }

  private static void configurePom(Project project, MavenPom pom) {
    MavenPublishPom publishPom = MavenPublishPom.fromProject(project)

    pom.groupId = publishPom.groupId
    pom.artifactId = publishPom.artifactId
    pom.version = publishPom.version

    pom.project {
      if (publishPom.name != null) {
        name publishPom.name
      }
      if (publishPom.packaging != null) {
        packaging publishPom.packaging
      }
      if (publishPom.description != null) {
        description publishPom.description
      }
      if (publishPom.url != null) {
        url publishPom.url
      }

      scm {
        if (publishPom.scmUrl != null) {
          url publishPom.scmUrl
        }
        if (publishPom.scmConnection != null) {
          connection publishPom.scmConnection
        }
        if (publishPom.scmDeveloperConnection != null) {
          developerConnection publishPom.scmDeveloperConnection
        }
      }

      licenses {
        license {
          if (publishPom.licenseName != null) {
            name publishPom.licenseName
          }
          if (publishPom.licenseUrl != null) {
            url publishPom.licenseUrl
          }
          if (publishPom.licenseDistribution != null) {
            distribution publishPom.licenseDistribution
          }
        }
      }

      developers {
        developer {
          if (publishPom.developerId != null) {
            id publishPom.developerId
          }
          if (publishPom.developerName != null) {
            name publishPom.developerName
          }
          if (publishPom.developerUrl != null) {
            url publishPom.developerUrl
          }
        }
      }
    }
  }
}
