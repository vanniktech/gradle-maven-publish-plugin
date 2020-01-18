package com.vanniktech.maven.publish

import org.assertj.core.api.Java6Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class MavenPublishPluginIntegrationTest(
  private val mavenPublishTargetTaskName: String,
  private val useMavenPublish: Boolean
) {
  companion object {
    const val TEST_GROUP = "com.example"
    const val TEST_VERSION_NAME = "1.0.0"
    const val TEST_POM_ARTIFACT_ID = "test-artifact"

    @JvmStatic @Parameterized.Parameters fun mavenPublishTargetsToTest() = listOf(
      arrayOf("installArchives", false),
      arrayOf("uploadArchives", false),
      arrayOf("installArchives", true),
      arrayOf("uploadArchives", true)
    )
  }

  @get:Rule val testProjectDir: TemporaryFolder = TemporaryFolder()

  private lateinit var repoFolder: File
  private lateinit var buildFile: File
  private lateinit var artifactFolder: String

  @Before fun setUp() {
    repoFolder = testProjectDir.newFolder("repo")
    buildFile = testProjectDir.newFile("build.gradle")
    buildFile.writeText("""
        plugins {
          id "com.vanniktech.maven.publish"
        }

        mavenPublish {
          useMavenPublish = $useMavenPublish
          targets {
            installArchives {
              releaseRepositoryUrl = "file://${repoFolder.absolutePath}"
              signing = true
            }
            uploadArchives {
              releaseRepositoryUrl = "file://${repoFolder.absolutePath}"
              signing = true
            }
          }
        }
        """)

    testProjectDir.newFile("gradle.properties").writeText("""
        GROUP=$TEST_GROUP
        VERSION_NAME=$TEST_VERSION_NAME
        POM_ARTIFACT_ID=$TEST_POM_ARTIFACT_ID

        signing.keyId=B89C4055
        signing.password=test
        signing.secretKeyRingFile=secring.gpg
        """)
    File("src/integrationTest/fixtures/test-secring.gpg").copyTo(File(testProjectDir.root, "secring.gpg"))

    val group = TEST_GROUP.replace(".", "/")
    val artifactId = TEST_POM_ARTIFACT_ID
    val version = TEST_VERSION_NAME
    artifactFolder = "${repoFolder.absolutePath}/$group/$artifactId/$version"
  }

  @Test fun generatesArtifactsAndDocumentationOnJavaProject() {
    buildFile.appendText("""
        apply plugin: "java"
        """)

    setupFixture("passing_java_project")

    val result = executeGradleCommands(mavenPublishTargetTaskName, "--info")

    assertThat(result.task(":$mavenPublishTargetTaskName")?.outcome).isEqualTo(SUCCESS)
    assertExpectedCommonArtifactsGenerated()
  }

  @Test fun generatesArtifactsAndDocumentationOnJavaLibraryProject() {
    buildFile.appendText("""
        apply plugin: "java-library"
        """)

    setupFixture("passing_java_library_project")

    val result = executeGradleCommands(mavenPublishTargetTaskName, "--info")

    assertThat(result.task(":$mavenPublishTargetTaskName")?.outcome).isEqualTo(SUCCESS)
    assertExpectedCommonArtifactsGenerated()
  }

  @Test fun generatesArtifactsAndDocumentationOnJavaLibraryWithGroovyProject() {
    buildFile.appendText("""
        apply plugin: "java-library"
        apply plugin: "groovy"
        sourceSets {
            main {
                groovy {
                    srcDirs = ['src/main/groovy']
                }
            }
        }

        repositories {
            mavenCentral()
        }

        dependencies {
            compile 'org.codehaus.groovy:groovy-all:2.5.6'
        }
        """)

    setupFixture("passing_java_library_with_groovy_project")

    val result = executeGradleCommands(mavenPublishTargetTaskName, "--info")

    assertThat(result.task(":$mavenPublishTargetTaskName")?.outcome).isEqualTo(SUCCESS)
    assertExpectedCommonArtifactsGenerated()
    assertArtifactGenerated("$TEST_POM_ARTIFACT_ID-$TEST_VERSION_NAME-groovydoc.jar")
  }

  /**
   * Copies test fixture into temp directory under test.
   */
  private fun setupFixture(fixtureName: String) {
    File("src/integrationTest/fixtures/$fixtureName").copyRecursively(testProjectDir.root)
  }

  /**
   * Makes sure common artifacts are generated (POM, javadoc, sources, etc.),
   * no matter what project type is and which plugins are applied.
   */
  private fun assertExpectedCommonArtifactsGenerated() {
    val artifactJar = "$TEST_POM_ARTIFACT_ID-$TEST_VERSION_NAME.jar"
    val pomFile = "$TEST_POM_ARTIFACT_ID-$TEST_VERSION_NAME.pom"
    val javadocJar = "$TEST_POM_ARTIFACT_ID-$TEST_VERSION_NAME-javadoc.jar"
    val sourcesJar = "$TEST_POM_ARTIFACT_ID-$TEST_VERSION_NAME-sources.jar"
    assertArtifactGenerated(artifactJar)
    assertArtifactGenerated(pomFile)
    assertArtifactGenerated(javadocJar)
    assertArtifactGenerated(sourcesJar)
  }

  private fun assertArtifactGenerated(artifactFileNameWithExtension: String) {
    assertThat(File("$artifactFolder/$artifactFileNameWithExtension")).exists()
    assertThat(File("$artifactFolder/$artifactFileNameWithExtension.asc")).exists()
  }

  private fun executeGradleCommands(vararg commands: String) = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments(*commands)
      .withPluginClasspath()
      .build()
}
