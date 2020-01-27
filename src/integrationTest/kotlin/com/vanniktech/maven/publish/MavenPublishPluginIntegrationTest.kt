package com.vanniktech.maven.publish

import org.assertj.core.api.Java6Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.io.File
import java.util.zip.ZipFile

@RunWith(Parameterized::class)
class MavenPublishPluginIntegrationTest(
  private val uploadArchivesTargetTaskName: String,
  private val mavenPublishTargetTaskName: String,
  private val useLegacyMode: Boolean
) {
  companion object {
    const val FIXTURES = "src/integrationTest/fixtures"
    const val EXPECTED_POM = "expected/test-artifact.pom"

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
  private lateinit var artifactFolder: File

  @Before fun setUp() {
    repoFolder = testProjectDir.newFolder("repo")

    File("$FIXTURES/common").listFiles()?.forEach { it.copyRecursively(testProjectDir.root.resolve(it.name)) }

    val group = TEST_GROUP.replace(".", "/")
    val artifactId = TEST_POM_ARTIFACT_ID
    val version = TEST_VERSION_NAME
    artifactFolder = repoFolder.resolve("$group/$artifactId/$version")
  }

  @Test fun generatesArtifactsAndDocumentationOnJavaProject() {
    setupFixture("passing_java_project")

    val result = executeGradleCommands(uploadArchivesTargetTaskName, "--info")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated("jar")
    assertSourceJarContainsFile("com/vanniktech/maven/publish/test/TestClass.java", "src/main/java")
  }

  @Test fun generatesArtifactsAndDocumentationOnJavaWithKotlinProject() {
    setupFixture("passing_java_with_kotlin_project")

    val result = executeGradleCommands(uploadArchivesTargetTaskName, "--info")

    assertExpectedTasksRanSuccessfully(result)
    assertThat(result.task(":dokka")?.outcome).isEqualTo(SUCCESS)
    assertExpectedCommonArtifactsGenerated("jar")
    assertSourceJarContainsFile("com/vanniktech/maven/publish/test/TestClass.kt", "src/main/java")
    assertSourceJarContainsFile("com/vanniktech/maven/publish/test/JavaTestClass.java", "src/main/java")
  }

  @Test fun generatesArtifactsAndDocumentationOnJavaLibraryProject() {
    setupFixture("passing_java_library_project")

    val result = executeGradleCommands(uploadArchivesTargetTaskName, "--info")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated("jar")
    assertSourceJarContainsFile("com/vanniktech/maven/publish/test/TestClass.java", "src/main/java")
  }

  @Test fun generatesArtifactsAndDocumentationOnJavaLibraryWithKotlinProject() {
    setupFixture("passing_java_library_with_kotlin_project")

    val result = executeGradleCommands(uploadArchivesTargetTaskName, "--info")

    assertExpectedTasksRanSuccessfully(result)
    assertThat(result.task(":dokka")?.outcome).isEqualTo(SUCCESS)
    assertExpectedCommonArtifactsGenerated("jar")
    assertSourceJarContainsFile("com/vanniktech/maven/publish/test/TestClass.kt", "src/main/java")
    assertSourceJarContainsFile("com/vanniktech/maven/publish/test/JavaTestClass.java", "src/main/java")
  }

  @Test fun generatesArtifactsAndDocumentationOnJavaLibraryWithGroovyProject() {
    setupFixture("passing_java_library_with_groovy_project")

    val result = executeGradleCommands(uploadArchivesTargetTaskName, "--info")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated("jar")
    assertArtifactGenerated("$TEST_POM_ARTIFACT_ID-$TEST_VERSION_NAME-groovydoc.jar")
    assertSourceJarContainsFile("com/vanniktech/maven/publish/test/TestClass.groovy", "src/main/groovy")
    assertSourceJarContainsFile("com/vanniktech/maven/publish/test/TestClass.java", "src/main/java")
  }

  @Test fun generatesArtifactsAndDocumentationOnAndroidProject() {
    setupFixture("passing_android_project")

    val result = executeGradleCommands(uploadArchivesTargetTaskName, "--info")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated("aar")
    assertSourceJarContainsFile("com/vanniktech/maven/publish/test/TestActivity.java", "src/main/java")
  }

  @Test fun generatesArtifactsAndDocumentationOnAndroidWithKotlinProject() {
    setupFixture("passing_android_with_kotlin_project")

    val result = executeGradleCommands(uploadArchivesTargetTaskName, "--info")

    assertExpectedTasksRanSuccessfully(result)
    assertThat(result.task(":dokka")?.outcome).isEqualTo(SUCCESS)
    assertExpectedCommonArtifactsGenerated("aar")
    assertSourceJarContainsFile("com/vanniktech/maven/publish/test/TestActivity.kt", "src/main/java")
    assertSourceJarContainsFile("com/vanniktech/maven/publish/test/JavaTestActivity.java", "src/main/java")
  }

  @Test fun generatesArtifactsAndDocumentationOnMinimalPomProject() {
    setupFixture("minimal_pom_project")

    val result = executeGradleCommands(uploadArchivesTargetTaskName, "--info")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated("jar")
  }

  /**
   * Copies test fixture into temp directory under test.
   */
  private fun setupFixture(fixtureName: String) {
    File("$FIXTURES/$fixtureName").copyRecursively(testProjectDir.root, overwrite = true)

    testProjectDir.root.resolve("gradle.properties").appendText("""
        GROUP=$TEST_GROUP
        VERSION_NAME=$TEST_VERSION_NAME
        POM_ARTIFACT_ID=$TEST_POM_ARTIFACT_ID

        test.releaseRepository=$repoFolder
        test.useLegacyMode=$useLegacyMode
        """)
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

    assertPomContentMatches(pomFile)
  }

  private fun assertArtifactGenerated(artifactFileNameWithExtension: String) {
    assertThat(artifactFolder.resolve(artifactFileNameWithExtension)).exists()
    assertThat(artifactFolder.resolve("$artifactFileNameWithExtension.asc")).exists()
  }

  private fun assertPomContentMatches(pomFile: String) {
    // in legacyMode for Android the packaging is written, for all other modes it's currently not written
    // https://github.com/vanniktech/gradle-maven-publish-plugin/issues/82
    val resolvedPomFile = artifactFolder.resolve(pomFile)
    val lines = resolvedPomFile.readLines()
    if (lines.contains("  <packaging>aar</packaging>")) {
      resolvedPomFile.writeText("")
      lines.forEach { line ->
        if (line != "  <packaging>aar</packaging>") {
          resolvedPomFile.appendText(line)
          resolvedPomFile.appendText("\n")
        }
      }
    }
    assertThat(resolvedPomFile).hasSameContentAs(testProjectDir.root.resolve(EXPECTED_POM))
  }

  private fun assertSourceJarContainsFile(file: String, srcRoot: String) {
    val sourcesJar = ZipFile(artifactFolder.resolve("$TEST_POM_ARTIFACT_ID-$TEST_VERSION_NAME-sources.jar"))
    val entry = sourcesJar.getEntry(file)
    val content = sourcesJar.getInputStream(entry)?.reader()?.buffered()?.readText()

    val expected = testProjectDir.root.resolve(srcRoot).resolve(file)
    val expectedContent = expected.readText()

    assertThat(content).isNotBlank()
    assertThat(content).isEqualTo(expectedContent)
  }

  private fun executeGradleCommands(vararg commands: String) = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments(*commands)
      .withPluginClasspath()
      .build()
}
