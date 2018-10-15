package com.vanniktech.maven.publish

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.artifacts.maven.MavenPom
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc

class MavenPublishPlugin implements Plugin<Project> {
  @Override void apply(final Project p) {
    def extension = p.extensions.create('mavenPublish', MavenPublishPluginExtension.class, p)

    p.group = p.findProperty("GROUP")
    p.version = p.findProperty("VERSION_NAME")

    p.afterEvaluate { Project project ->
      Configurer configurer
      if (extension.useMavenPublish) {
        configurer = new MavenPublishConfigurer(p)
      } else {
        configurer = new GroovyUploadArchivesConfigurer(p, extension)
      }

      extension.targets.each { target ->
        if (target.releaseRepositoryUrl == null) {
          throw new IllegalStateException("The release repository url of ${target.name} is null or not set")
        }

        configurer.configureTarget(target)
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

        if (extension.useMavenPublish) {
          throw IllegalArgumentException("Using maven-publish for Android libraries is currently unsupported.")
        }
        configurer.addTaskOutput(project.androidSourcesJar)
        configurer.addTaskOutput(project.androidJavadocsJar)
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

        configurer.addComponent(project.components.java)
        configurer.addTaskOutput(project.jar)
        configurer.addTaskOutput(project.javadocsJar)
        if (plugins.hasPlugin('groovy')) {
          configurer.addTaskOutput(project.groovydocJar)
        }
        configurer.addTaskOutput(project.sourcesJar)
      }

      if (JavaVersion.current().isJava8Compatible()) {
        project.allprojects {
          tasks.withType(Javadoc) {
            options.addStringOption('Xdoclint:none', '-quiet')
          }
        }
      }
    }
  }

  static void configureMavenDeployer(Project project, Upload upload, MavenPublishTarget target) {
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
      name publishPom.name
      packaging publishPom.packaging
      description publishPom.description
      url publishPom.url

      scm {
        url publishPom.scmUrl
        connection publishPom.scmConnection
        developerConnection publishPom.scmDeveloperConnection
      }

      licenses {
        license {
          name publishPom.licenseName
          url publishPom.licenseUrl
          distribution publishPom.licenseDistribution
        }
      }

      developers {
        developer {
          id publishPom.developerId
          name publishPom.developerName
        }
      }
    }
  }
}
