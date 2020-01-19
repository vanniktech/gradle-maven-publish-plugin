package com.vanniktech.maven.publish

import org.assertj.core.api.Java6Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.io.File

@RunWith(Parameterized::class)
class MavenPublishPluginIntegrationTest(
  private val uploadArchivesTargetTaskName: String,
  private val mavenPublishTargetTaskName: String,
  private val useLegacyMode: Boolean
) {
  companion object {
    const val TEST_GROUP = "com.example"
    const val TEST_VERSION_NAME = "1.0.0"
    const val TEST_POM_ARTIFACT_ID = "test-artifact"

    @JvmStatic
    @Parameters(name = "{0} with legacyMode={2}")
    fun mavenPublishTargetsToTest() = listOf(
      arrayOf("installArchives", "publishMavenPublicationToLocalRepository", false),
      arrayOf("uploadArchives", "publishMavenPublicationToMavenRepository", false),
      arrayOf("installArchives", "publishMavenPublicationToLocalRepository", true),
      arrayOf("uploadArchives", "publishMavenPublicationToMavenRepository", true)
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

        repositories {
            google()
            mavenCentral()
            jcenter()
        }

        mavenPublish {
          useLegacyMode = $useLegacyMode
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

    val result = executeGradleCommands(uploadArchivesTargetTaskName, "--info")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated("jar")
  }

  @Test fun generatesArtifactsAndDocumentationOnJavaLibraryProject() {
    buildFile.appendText("""
        apply plugin: "java-library"
        """)

    setupFixture("passing_java_library_project")

    val result = executeGradleCommands(uploadArchivesTargetTaskName, "--info")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated("jar")
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

    val result = executeGradleCommands(uploadArchivesTargetTaskName, "--info")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated("jar")
    assertArtifactGenerated("$TEST_POM_ARTIFACT_ID-$TEST_VERSION_NAME-groovydoc.jar")
  }

  @Test fun generatesArtifactsAndDocumentationOnAndroidProject() {
    assumeTrue(useLegacyMode)

    val currentBuildFile = buildFile.readText()
    buildFile.writeText("""
        plugins {
          id "com.android.library"
        }
        """)
    buildFile.appendText(currentBuildFile)
    buildFile.appendText("""
        android {
          compileSdkVersion 29
        }
        """)

    setupFixture("passing_android_project")

    val result = executeGradleCommands(uploadArchivesTargetTaskName, "--info")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated("aar")
  }

  /**
   * Copies test fixture into temp directory under test.
   */
  private fun setupFixture(fixtureName: String) {
    File("src/integrationTest/fixtures/$fixtureName").copyRecursively(testProjectDir.root)
  }

  private fun assertExpectedTasksRanSuccessfully(result: BuildResult) {
    assertThat(result.task(":$uploadArchivesTargetTaskName")?.outcome).isEqualTo(SUCCESS)
    if (useLegacyMode) {
      assertThat(result.task(":$mavenPublishTargetTaskName")).isNull()
    } else {
      assertThat(result.task(":$mavenPublishTargetTaskName")?.outcome).isEqualTo(SUCCESS)
    }
  }

  /**
   * Makes sure common artifacts are generated (POM, javadoc, sources, etc.),
   * no matter what project type is and which plugins are applied.
   */
  private fun assertExpectedCommonArtifactsGenerated(artifactExtension: String) {
    val artifactJar = "$TEST_POM_ARTIFACT_ID-$TEST_VERSION_NAME.$artifactExtension"
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
