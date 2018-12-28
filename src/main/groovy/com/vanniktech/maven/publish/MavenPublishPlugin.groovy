package com.vanniktech.maven.publish

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.artifacts.maven.MavenPom
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.jetbrains.annotations.NotNull

class MavenPublishPlugin extends BaseMavenPublishPlugin {

  @Override
  protected void setupConfigurerForAndroid(@NotNull Project project, @NotNull Configurer configurer) {
    PluginContainer plugins = project.plugins
    MavenPublishPluginExtension extension = project.extensions.getByType(MavenPublishPluginExtension.class)

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

    def androidJavadocsJar = project.tasks.create("androidJavadocsJar", Jar.class) {
      classifier = 'javadoc'
    }

    if (plugins.hasPlugin('kotlin-android')) {
      def dokkaOutput = "${project.docsDir}/dokka"
      project.plugins.apply('org.jetbrains.dokka-android')
      project.dokka {
        outputFormat 'javadoc'
        outputDirectory dokkaOutput
      }
      androidJavadocsJar.dependsOn("dokka")
      androidJavadocsJar.configure {
        dependsOn "dokka"
        from dokkaOutput
      }
    } else {
      androidJavadocsJar.configure {
        dependsOn "androidJavadocs"
        from project.androidJavadocs.destinationDir
      }
    }

    project.tasks.create("androidSourcesJar", Jar.class) {
      classifier = 'sources'
      from project.android.sourceSets.main.java.sourceFiles
    }

    if (extension.useMavenPublish) {
      throw IllegalArgumentException("Using maven-publish for Android libraries is currently unsupported.")
    }
    configurer.addTaskOutput(project.androidSourcesJar)
    configurer.addTaskOutput(project.androidJavadocsJar)
  }

  @Override
  protected void setupConfigurerForJava(@NotNull Project project, @NotNull Configurer configurer) {
    PluginContainer plugins = project.plugins

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

    def javadocsJar = project.tasks.create("javadocsJar", Jar.class) {
      classifier = 'javadoc'
    }

    if (project.plugins.hasPlugin("kotlin")) {
      def dokkaOutput = "${project.docsDir}/dokka"
      project.plugins.apply('org.jetbrains.dokka')
      project.dokka {
        outputFormat 'javadoc'
        outputDirectory dokkaOutput
      }
      javadocsJar.configure {
        dependsOn "dokka"
        from dokkaOutput
      }
    } else {
      javadocsJar.configure {
        dependsOn "javadoc"
        from project.javadoc.destinationDir
      }
    }

    configurer.addComponent(project.components.java)
    configurer.addTaskOutput(project.jar)
    configurer.addTaskOutput(project.javadocsJar)
    if (plugins.hasPlugin('groovy')) {
      configurer.addTaskOutput(project.groovydocJar)
    }
    configurer.addTaskOutput(project.sourcesJar)
  }

  @Override
  protected void java8Javadoc(Project project) {
    if (JavaVersion.current().isJava8Compatible()) {
      project.allprojects {
        tasks.withType(Javadoc) {
          options.addStringOption('Xdoclint:none', '-quiet')
        }
      }
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
