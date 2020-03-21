package com.vanniktech.maven.publish

import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.artifacts.maven.MavenPom
import org.gradle.api.tasks.Upload

class MavenPublishPlugin extends BaseMavenPublishPlugin {

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

    pom.groupId = project.group
    pom.artifactId = publishPom.artifactId
    pom.version = project.version

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
