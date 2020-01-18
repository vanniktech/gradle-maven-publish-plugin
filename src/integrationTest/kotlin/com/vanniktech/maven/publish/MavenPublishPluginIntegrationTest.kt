package com.vanniktech.maven.publish

import org.assertj.core.api.Java6Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Assume.assumeFalse
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
  private val useMavenPublish: Boolean
) {
  companion object {
    const val FIXTURES = "src/integrationTest/fixtures"
    const val EXPECTED_POM = "expected/test-artifact.pom"

    const val TEST_GROUP = "com.example"
    const val TEST_VERSION_NAME = "1.0.0"
    const val TEST_POM_ARTIFACT_ID = "test-artifact"

    @JvmStatic
    @Parameters(name = "{0} with useMavenPublish={2}")
    fun mavenPublishTargetsToTest() = listOf(
      arrayOf("installArchives", "publishMavenPublicationToLocalRepository", false),
      arrayOf("uploadArchives", "publishMavenPublicationToMavenRepository", false),
      arrayOf("installArchives", "publishMavenPublicationToLocalRepository", true),
      arrayOf("uploadArchives", "publishMavenPublicationToMavenRepository", true)
    )
  }

  @get:Rule val testProjectDir: TemporaryFolder = TemporaryFolder()

  private lateinit var repoFolder: File
  private lateinit var artifactFolder: String

  @Before fun setUp() {
    repoFolder = testProjectDir.newFolder("repo")

    File("$FIXTURES/common").listFiles()!!.forEach { it.copyRecursively(File(testProjectDir.root, it.name)) }
    File(testProjectDir.root, "gradle.properties").appendText("""
        GROUP=$TEST_GROUP
        VERSION_NAME=$TEST_VERSION_NAME
        POM_ARTIFACT_ID=$TEST_POM_ARTIFACT_ID

        test.releaseRepository=$repoFolder
        test.useMavenPublish=$useMavenPublish
        """)

    val group = TEST_GROUP.replace(".", "/")
    val artifactId = TEST_POM_ARTIFACT_ID
    val version = TEST_VERSION_NAME
    artifactFolder = "${repoFolder.absolutePath}/$group/$artifactId/$version"
  }

  @Test fun generatesArtifactsAndDocumentationOnJavaProject() {
    setupFixture("passing_java_project")

    val result = executeGradleCommands(uploadArchivesTargetTaskName, "--info")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated("jar")
  }

  @Test fun generatesArtifactsAndDocumentationOnJavaLibraryProject() {
    setupFixture("passing_java_library_project")

    val result = executeGradleCommands(uploadArchivesTargetTaskName, "--info")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated("jar")
  }

  @Test fun generatesArtifactsAndDocumentationOnJavaLibraryWithGroovyProject() {
    setupFixture("passing_java_library_with_groovy_project")

    val result = executeGradleCommands(uploadArchivesTargetTaskName, "--info")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated("jar")
    assertArtifactGenerated("$TEST_POM_ARTIFACT_ID-$TEST_VERSION_NAME-groovydoc.jar")
  }

  @Test fun generatesArtifactsAndDocumentationOnAndroidProject() {
    assumeFalse(useMavenPublish)

    setupFixture("passing_android_project")

    val result = executeGradleCommands(uploadArchivesTargetTaskName, "--info")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated("aar")
  }

  /**
   * Copies test fixture into temp directory under test.
   */
  private fun setupFixture(fixtureName: String) {
    File("$FIXTURES/$fixtureName").copyRecursively(testProjectDir.root, overwrite = true)
  }

  private fun assertExpectedTasksRanSuccessfully(result: BuildResult) {
    assertThat(result.task(":$uploadArchivesTargetTaskName")?.outcome).isEqualTo(SUCCESS)
    if (useMavenPublish) {
      assertThat(result.task(":$mavenPublishTargetTaskName")?.outcome).isEqualTo(SUCCESS)
    } else {
      assertThat(result.task(":$mavenPublishTargetTaskName")).isNull()
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

    assertThat(File("$artifactFolder/$pomFile")).hasSameContentAs(File(testProjectDir.root, EXPECTED_POM))
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
