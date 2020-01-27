package com.vanniktech.maven.publish

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.artifacts.maven.MavenPom
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.jetbrains.annotations.NotNull

class MavenPublishPlugin extends BaseMavenPublishPlugin {

  @Override
  protected void setupConfigurerForAndroid(@NotNull Project project, @NotNull Configurer configurer) {
    PluginContainer plugins = project.plugins
    MavenPublishPluginExtension extension = project.extensions.getByType(MavenPublishPluginExtension.class)

    if (!extension.useLegacyMode) {
      configurer.addComponent(project.components.getByName(extension.androidVariantToPublish))
    }

    // Append also the classpath and files for release library variants. This fixes the javadoc warnings.
    // Got it from here - https://github.com/novoda/bintray-release/pull/39/files
    def releaseVariantCompileProvider = project.android.libraryVariants.toList().last().javaCompileProvider
    TaskProvider<Javadoc> docTask = project.tasks.register("androidJavadocs", Javadoc.class) {
      dependsOn releaseVariantCompileProvider
      if (!plugins.hasPlugin('kotlin-android')) {
        source = project.android.sourceSets.main.java.srcDirs
      }

      failOnError true
      classpath += project.files(project.android.getBootClasspath().join(File.pathSeparator))
      // Safe to call get() here because we'ved marked this as dependent on the TaskProvider
      classpath += releaseVariantCompileProvider.get().classpath
      classpath += releaseVariantCompileProvider.get().outputs.files

      // We don't need javadoc for internals.
      exclude '**/internal/*'

      // Append Java 7, Android references and docs.
      options.links("http://docs.oracle.com/javase/7/docs/api/");
      options.linksOffline "https://developer.android.com/reference", "${project.android.sdkDirectory}/docs/reference"
    }

    def androidJavadocsJar = project.tasks.register("androidJavadocsJar", Jar.class) {
      classifier = 'javadoc'
    }
    configurer.addTaskOutput(androidJavadocsJar)

    if (plugins.hasPlugin('kotlin-android')) {
      def dokkaOutput = "${project.docsDir}/dokka"
      project.plugins.apply('org.jetbrains.dokka')
      project.dokka {
        outputFormat 'html'
        outputDirectory dokkaOutput
      }
      androidJavadocsJar.configure {
        dependsOn "dokka"
        from dokkaOutput
      }
    } else {
      androidJavadocsJar.configure {
        dependsOn docTask
        from project.androidJavadocs.destinationDir
      }
    }

    def androidSourcesJar = project.tasks.register("androidSourcesJar", Jar.class) {
      classifier = 'sources'
      from project.android.sourceSets.main.java.srcDirs
    }
    configurer.addTaskOutput(androidSourcesJar)
  }

  @Override
  protected void setupConfigurerForJava(@NotNull Project project, @NotNull Configurer configurer) {

    configurer.addComponent(project.components.java)

    PluginContainer plugins = project.plugins

    if (plugins.hasPlugin('groovy')) {
      def goovydocJar = project.tasks.register("groovydocJar", Jar.class) {
        dependsOn project.tasks.named("groovydoc")
        classifier = 'groovydoc'
        from project.groovydoc.destinationDir
      }
      configurer.addTaskOutput(goovydocJar)
    }

    def sourcesJar = project.tasks.register("sourcesJar", Jar.class) {
      dependsOn project.tasks.named("classes")
      classifier = 'sources'
      from project.sourceSets.main.allSource
    }
    configurer.addTaskOutput(sourcesJar)

    def javadocsJar = project.tasks.register("javadocsJar", Jar.class) {
      classifier = 'javadoc'
    }
    configurer.addTaskOutput(javadocsJar)

    if (project.plugins.hasPlugin("kotlin")) {
      def dokkaOutput = "${project.docsDir}/dokka"
      project.plugins.apply('org.jetbrains.dokka')
      project.dokka {
        outputFormat 'html'
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
