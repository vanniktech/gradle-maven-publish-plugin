package com.vanniktech.maven.publish

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import org.assertj.core.api.Java6Assertions.assertThat
import org.gradle.api.Project
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.MavenPlugin
import org.gradle.api.tasks.Upload
import org.gradle.plugins.signing.SigningPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test
import java.io.File

class MavenPublishPluginTest {

  private lateinit var project: Project

  @Before
  fun setUp() {
    project = ProjectBuilder.builder().withName("project").build()
    project.extensions.extraProperties.set("GROUP", "com.example")
    project.extensions.extraProperties.set("VERSION_NAME", "1.0.0")
  }

  @Test fun javaPlugin() {
    project.plugins.apply(JavaPlugin::class.java)
    assert(project)
  }

  @Test fun javaLibraryPlugin() {
    project.plugins.apply(JavaLibraryPlugin::class.java)
    assert(project)
  }

  @Test fun javaLibraryPluginWithGroovy() {
    project.plugins.apply(JavaLibraryPlugin::class.java)
    project.plugins.apply(GroovyPlugin::class.java)
    assert(project)

    assertThat(project.tasks.getByName("groovydocJar")).isNotNull()
  }

  @Test fun androidLibraryPlugin() {
    project.plugins.apply(LibraryPlugin::class.java)

    prepareAndroidLibraryProject(project)

    assert(project)
  }

  @Test fun androidLibraryPluginWithKotlinAndroid() {
    project.plugins.apply(LibraryPlugin::class.java)
    project.plugins.apply("kotlin-android")

    prepareAndroidLibraryProject(project)

    assert(project)
  }

  @Test fun javaLibraryPluginWithKotlin() {
    project.plugins.apply(JavaLibraryPlugin::class.java)
    project.plugins.apply("kotlin")
    assert(project)
  }

  private fun prepareAndroidLibraryProject(project: Project) {
    val extension = project.extensions.getByType(LibraryExtension::class.java)
    extension.compileSdkVersion(27)

    val manifestFile = File(project.projectDir, "src/main/AndroidManifest.xml")
    manifestFile.parentFile.mkdirs()
    manifestFile.writeText("""<manifest package="com.foo.bar"/>""")
  }

  // This does not assert anything but it's a good start.
  private fun assert(project: Project) {
    project.plugins.apply(MavenPublishPlugin::class.java)

    val extension = project.extensions.getByType(MavenPublishPluginExtension::class.java)
    extension.targets.getByName("uploadArchives").repositoryUsername = "bar"
    extension.targets.getByName("uploadArchives").repositoryPassword = "foo"

    (project as DefaultProject).evaluate()

    assertThat(project.plugins.findPlugin(MavenPlugin::class.java)).isNotNull()
    assertThat(project.plugins.findPlugin(SigningPlugin::class.java)).isNotNull()
    assertThat(project.group).isNotNull()
    assertThat(project.version).isNotNull()

    val uploadArchives = project.tasks.getByName("uploadArchives")
    assertThat(uploadArchives.description).isEqualTo("Uploads all artifacts belonging to configuration ':archives'")
    assertThat(uploadArchives.group).isEqualTo("upload")

    val installArchives = project.tasks.getByName("installArchives") as Upload
    assertThat(installArchives.description).isEqualTo("Installs the artifacts to the local Maven repository.")
    assertThat(installArchives.group).isEqualTo("upload")
    assertThat(installArchives.configuration).isEqualTo(project.configurations.getByName("archives"))
  }
}
