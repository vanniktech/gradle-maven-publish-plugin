package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.tasks.AndroidJavadocs
import com.vanniktech.maven.publish.tasks.AndroidJavadocsJar
import com.vanniktech.maven.publish.tasks.AndroidSourcesJar
import com.vanniktech.maven.publish.tasks.EmptySourcesJar
import com.vanniktech.maven.publish.tasks.GroovydocsJar
import com.vanniktech.maven.publish.tasks.JavadocsJar
import com.vanniktech.maven.publish.tasks.SourcesJar
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

@Suppress("TooManyFunctions")
internal class MavenPublishConfigurer(
  private val project: Project
) {

  private fun configurePom(publication: MavenPublication) {
    @Suppress("UnstableApiUsage")
    publication.pom { pom ->
      pom.name.set(project.findOptionalProperty("POM_NAME"))
      pom.description.set(project.findOptionalProperty("POM_DESCRIPTION"))
      pom.url.set(project.findOptionalProperty("POM_URL"))
      pom.inceptionYear.set(project.findOptionalProperty("POM_INCEPTION_YEAR"))

      pom.scm {
        it.url.set(project.findOptionalProperty("POM_SCM_URL"))
        it.connection.set(project.findOptionalProperty("POM_SCM_CONNECTION"))
        it.developerConnection.set(project.findOptionalProperty("POM_SCM_DEV_CONNECTION"))
      }

      pom.licenses { licenses ->
        licenses.license {
          it.name.set(project.findOptionalProperty("POM_LICENCE_NAME"))
          it.url.set(project.findOptionalProperty("POM_LICENCE_URL"))
          it.distribution.set(project.findOptionalProperty("POM_LICENCE_DIST"))
        }
      }

      pom.developers { developers ->
        developers.developer {
          it.id.set(project.findOptionalProperty("POM_DEVELOPER_ID"))
          it.name.set(project.findOptionalProperty("POM_DEVELOPER_NAME"))
          it.url.set(project.findOptionalProperty("POM_DEVELOPER_URL"))
        }
      }
    }
  }

  fun configureGradlePluginProject() {
    val sourcesJar = project.tasks.register(SOURCES_TASK, SourcesJar::class.java)
    val javadocsJar = project.tasks.register(JAVADOC_TASK, JavadocsJar::class.java)

    project.publishing.publications.withType(MavenPublication::class.java).all {
      if (it.name == "pluginMaven") {
        configurePom(it)
        it.artifact(javadocsJar)
        it.artifact(sourcesJar)
      }

      project.extensions.getByType(GradlePluginDevelopmentExtension::class.java).plugins.forEach { plugin ->
        if (it.name == "${plugin.name}PluginMarkerMaven") {
          // keep the current group and artifact ids, they are based on the gradle plugin id
          configurePom(it)
        }
      }
    }
  }

  fun configureKotlinMppProject() {
    val javadocsJar = project.tasks.register(JAVADOC_TASK, JavadocsJar::class.java)

    project.publishing.publications.withType(MavenPublication::class.java).all {
      configurePom(it)
      it.artifact(javadocsJar)

      // Source jars are only created for platforms, not the common artifact.
      if (it.name == "kotlinMultiplatform") {
        val sourceArtifact = it.artifacts.find { artifact -> artifact.classifier == "sources" }
        if (sourceArtifact == null) {
          val emptySourcesJar = project.tasks.register("emptySourcesJar", EmptySourcesJar::class.java)
          it.artifact(emptySourcesJar)
        }
      }
    }
  }

  fun configureKotlinJsProject() {
    val javadocsJar = project.tasks.register(JAVADOC_TASK, JavadocsJar::class.java)

    // Create publication, since Kotlin/JS doesn't provide one by default.
    // https://youtrack.jetbrains.com/issue/KT-41582
    project.publishing.publications.create("mavenJs", MavenPublication::class.java) {
      configurePom(it)
      it.from(project.components.getByName("kotlin"))
      it.artifact(project.tasks.named("kotlinSourcesJar"))
      it.artifact(javadocsJar)
    }
  }

  fun configureAndroidArtifacts() {
    val publications = project.publishing.publications
    publications.create(PUBLICATION_NAME, MavenPublication::class.java) { publication ->
      configurePom(publication)
    }

    val publication = project.publishing.publications.getByName(PUBLICATION_NAME) as MavenPublication

    publication.from(project.components.getByName(project.publishExtension.androidVariantToPublish))

    val androidSourcesJar = project.tasks.register("androidSourcesJar", AndroidSourcesJar::class.java)
    publication.artifact(androidSourcesJar)

    project.tasks.register("androidJavadocs", AndroidJavadocs::class.java)
    val androidJavadocsJar = project.tasks.register("androidJavadocsJar", AndroidJavadocsJar::class.java)
    publication.artifact(androidJavadocsJar)
  }

  fun configureJavaArtifacts() {
    val publications = project.publishing.publications
    publications.create(PUBLICATION_NAME, MavenPublication::class.java) { publication ->
      configurePom(publication)
    }

    val publication = project.publishing.publications.getByName(PUBLICATION_NAME) as MavenPublication

    publication.from(project.components.getByName("java"))

    val sourcesJar = project.tasks.register(SOURCES_TASK, SourcesJar::class.java)
    publication.artifact(sourcesJar)

    val javadocsJar = project.tasks.register(JAVADOC_TASK, JavadocsJar::class.java)
    publication.artifact(javadocsJar)

    if (project.plugins.hasPlugin("groovy")) {
      val goovydocsJar = project.tasks.register("groovydocJar", GroovydocsJar::class.java)
      publication.artifact(goovydocsJar)
    }
  }

  companion object {
    const val PUBLICATION_NAME = "maven"
    const val JAVADOC_TASK = "javadocsJar"
    const val SOURCES_TASK = "sourcesJar"
  }
}
