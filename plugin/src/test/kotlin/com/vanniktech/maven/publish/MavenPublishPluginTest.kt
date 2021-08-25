package com.vanniktech.maven.publish

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin as GradleMavenPublishPlugin
import org.gradle.plugins.signing.SigningPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class MavenPublishPluginTest {

  private lateinit var project: Project

  @Before
  fun setUp() {
    project = ProjectBuilder.builder().withName("project").build()
    project.extensions.extraProperties.set("GROUP", "com.example")
    project.extensions.extraProperties.set("VERSION_NAME", "1.0.0")
    project.extensions.extraProperties.set("POM_ARTIFACT_ID", "artifact")
  }

  @Test fun javaPlugin() {
    project.plugins.apply(JavaPlugin::class.java)
    assert(project)
  }

  @Test fun javaLibraryPlugin() {
    project.plugins.apply(JavaLibraryPlugin::class.java)
    assert(project)
  }

  @Test fun javaLibraryPluginWithKotlin() {
    project.plugins.apply(JavaLibraryPlugin::class.java)
    project.plugins.apply("kotlin")
    assert(project)
  }

  // This does not assert anything but it's a good start.
  private fun assert(project: Project) {
    project.plugins.apply(MavenPublishPlugin::class.java)

    (project as DefaultProject).evaluate()

    assertThat(project.plugins.findPlugin(GradleMavenPublishPlugin::class.java)).isNotNull()
    assertThat(project.plugins.findPlugin(SigningPlugin::class.java)).isNotNull()
    assertThat(project.group).isNotNull()
    assertThat(project.version).isNotNull()

    val uploadArchives = project.tasks.findByName("publish")
    assertThat(uploadArchives).isNotNull()

    val installArchives = project.tasks.findByName("publishToMavenLocal")
    assertThat(installArchives).isNotNull()
  }
}
