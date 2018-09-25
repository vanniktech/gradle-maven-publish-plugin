package com.vanniktech.maven.publish

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.artifacts.maven.MavenPom
import org.gradle.api.plugins.MavenPlugin
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.plugins.signing.SigningPlugin

class MavenPublishPlugin implements Plugin<Project> {
  @Override void apply(final Project p) {
    def extension = p.extensions.create('mavenPublish', MavenPublishPluginExtension.class, p)
    def extraRepos = p.extensions.create('mavenRepositories', MavenPublishRepositories.class)

    p.plugins.apply(MavenPlugin)
    p.plugins.apply(SigningPlugin)

    p.group = p.findProperty("GROUP")
    p.version = p.findProperty("VERSION_NAME")

    p.afterEvaluate { Project project ->
      configureMavenDeployer(project, (Upload) project.uploadArchives, extension.defaultTarget)

      extraRepos.map.each { key, value ->
        def taskName = "uploadArchives" + key.capitalize()
        project.logger.debug("Creating $taskName to upload to ${value.releaseRepositoryUrl} / ${value.snapshotRepositoryUrl}")
        project.tasks.create(taskName, Upload.class) {

          // add group and description.
          group = "upload"
          description = "Uploads all artifacts to ${key}"

          // Use archives configurations from the project
          configuration = project.configurations[Dependency.ARCHIVES_CONFIGURATION]

          // depends on same tasks as uploadArchives
          def defaultUploadArchives = project.uploadArchives as Upload
          dependsOn(defaultUploadArchives.dependsOn.clone())

          // setup repositories
          configureMavenDeployer(project, it, value)
        }
      }

      project.signing {
        required { !project.version.contains("SNAPSHOT") && project.gradle.taskGraph.hasTask("uploadArchives") }
        sign project.configurations.archives
      }

      def plugins = project.getPlugins()

      if (plugins.hasPlugin('com.android.library')) {
        project.tasks.create("androidJavadocs", Javadoc.class) {
          if (!plugins.hasPlugin('kotlin-android')) {
            source = project.android.sourceSets.main.java.srcDirs
          }

          failOnError true
          classpath += project.files(project.android.getBootClasspath().join(File.pathSeparator))

          // Append also the classpath and files for release library variants. This fixes the javadoc warnings.
          // Got it from here - https://github.com/novoda/bintray-release/pull/39/files
          def releaseVariant = project.android.libraryVariants.toList().last()
          classpath += releaseVariant.javaCompile.classpath
          classpath += releaseVariant.javaCompile.outputs.files

          // We don't need javadoc for internals.
          exclude '**/internal/*'

          // Append Java 7, Android references and docs.
          options.links("http://docs.oracle.com/javase/7/docs/api/");
          options.linksOffline "https://developer.android.com/reference", "${project.android.sdkDirectory}/docs/reference"
        }

        project.tasks.create("androidJavadocsJar", Jar.class) {
          classifier = 'javadoc'
          from project.androidJavadocs.destinationDir
        }.dependsOn("androidJavadocs")

        project.tasks.create("androidSourcesJar", Jar.class) {
          classifier = 'sources'
          from project.android.sourceSets.main.java.sourceFiles
        }

        project.artifacts {
          archives project.androidSourcesJar
          archives project.androidJavadocsJar
        }
      } else {
        if (plugins.hasPlugin('groovy')) {
          project.tasks.create("groovydocJar", Jar.class) {
            classifier = 'groovydoc'
            from project.groovydoc.destinationDir
          }.dependsOn("groovydoc")
        }

        project.tasks.create("sourcesJar", Jar.class) {
          classifier = 'sources'
          from project.sourceSets.main.allSource
        }.dependsOn("classes")

        project.tasks.create("javadocsJar", Jar.class) {
          classifier = 'javadoc'
          from project.javadoc.destinationDir
        }.dependsOn("javadoc")

        project.artifacts {
          archives project.jar
          archives project.javadocsJar

          if (plugins.hasPlugin('groovy')) {
            archives project.groovydocJar
          }

          archives project.sourcesJar
        }
      }

      if (JavaVersion.current().isJava8Compatible()) {
        project.allprojects {
          tasks.withType(Javadoc) {
            options.addStringOption('Xdoclint:none', '-quiet')
          }
        }
      }

      project.tasks.create("installArchives", Upload) {
        description = "Installs the artifacts to the local Maven repository."
        group = "upload"
        configuration = project.configurations['archives']

        configureMavenDeployer(project, it, extension.localTarget)
      }
    }
  }

  private void configureMavenDeployer(Project project, Upload upload, MavenPublishTarget target) {
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

  private void configurePom(Project project, MavenPom pom) {
    pom.groupId = project.findProperty("GROUP")
    pom.artifactId = project.findProperty("POM_ARTIFACT_ID")
    pom.version = project.findProperty("VERSION_NAME")

    pom.project {
      name project.findProperty("POM_NAME")
      packaging project.findProperty("POM_PACKAGING")
      description project.findProperty("POM_DESCRIPTION")
      url project.findProperty("POM_URL")

      scm {
        url project.findProperty("POM_SCM_URL")
        connection project.findProperty("POM_SCM_CONNECTION")
        developerConnection project.findProperty("POM_SCM_DEV_CONNECTION")
      }

      licenses {
        license {
          name project.findProperty("POM_LICENCE_NAME")
          url project.findProperty("POM_LICENCE_URL")
          distribution project.findProperty("POM_LICENCE_DIST")
        }
      }

      developers {
        developer {
          id project.findProperty("POM_DEVELOPER_ID")
          name project.findProperty("POM_DEVELOPER_NAME")
        }
      }
    }
  }
}
