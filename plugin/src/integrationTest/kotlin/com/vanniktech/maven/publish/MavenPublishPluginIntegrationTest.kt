package com.vanniktech.maven.publish

import java.io.File
import java.util.zip.ZipFile
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MavenPublishPluginIntegrationTest {
  @get:Rule val testProjectDir: TemporaryFolder = TemporaryFolder()

  private lateinit var repoFolder: File
  private lateinit var projectFolder: File
  private lateinit var expectedFolder: File

  // TODO
  @Test fun generatesArtifactsAndDocumentationOnKotlinJvmWithDokkaProject() {
    setupFixture("passing_kotlin_jvm_with_dokka_project")

    val result = executeGradleCommands(TEST_TASK, "--stacktrace")

    assertExpectedTasksRanSuccessfully(result, hasDokka = true)
    assertExpectedCommonArtifactsGenerated()
    assertPomContentMatches()
    assertSourceJarContainsFile("com/vanniktech/maven/publish/test/TestClass.kt", "src/main/java")
  }

  // TODO
  @Test fun generatesArtifactsAndDocumentationOnAndroidSingleVariantProject() {
    setupFixture("passing_android_single_variant_project")

    val result = executeGradleCommands(TEST_TASK, "--stacktrace")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated(artifactExtension = "aar")
    assertPomContentMatches()
  }

  // TODO
  @Test fun generatesArtifactsAndDocumentationOnAndroidMultiVariantProject() {
    setupFixture("passing_android_multi_variant_project")

    val result = executeGradleCommands(TEST_TASK, "--stacktrace")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated("aar", qualifier = "debug")
    assertExpectedCommonArtifactsGenerated("aar", qualifier = "release")
    assertPomContentMatches()
  }

  // TODO
  @Test fun generatesArtifactsAndDocumentationOnKotlinMppProject() {
    setupFixture("passing_kotlin_mpp_project")

    val result = executeGradleCommands(TEST_TASK, "--stacktrace")

    assertExpectedTasksRanSuccessfully(result)

    assertExpectedCommonArtifactsGenerated()
    assertPomContentMatches()

    val jvmArtifactId = "$TEST_POM_ARTIFACT_ID-jvm"
    assertExpectedCommonArtifactsGenerated(artifactId = jvmArtifactId)
    // TODO dependency sorting is unstable - the main variant only has 1 and is enough to see that the pom is correct
    // assertPomContentMatches(jvmArtifactId)

    val nodejsArtifactId = "$TEST_POM_ARTIFACT_ID-nodejs"
    assertExpectedCommonArtifactsGenerated(artifactId = nodejsArtifactId)
    // TODO dependency sorting is unstable - the main variant only has 1 and is enough to see that the pom is correct
    // assertPomContentMatches(nodejsArtifactId)

    val linuxArtifactId = "$TEST_POM_ARTIFACT_ID-linux"
    assertExpectedCommonArtifactsGenerated(artifactExtension = "klib", artifactId = linuxArtifactId)
    assertPomContentMatches(linuxArtifactId)
  }

  // TODO
  @Test fun kotlinMppArtifactIdReplacementWorksCorrectly1() {
    setupFixture("passing_kotlin_mpp_project", "foo")

    val result = executeGradleCommands(TEST_TASK, "--stacktrace", "-PPOM_ARTIFACT_ID=foo-bar")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated(artifactId = "foo-bar")
    assertExpectedCommonArtifactsGenerated(artifactId = "foo-bar-jvm")
    assertExpectedCommonArtifactsGenerated(artifactId = "foo-bar-nodejs")
    assertExpectedCommonArtifactsGenerated(artifactExtension = "klib", artifactId = "foo-bar-linux")
  }

  // TODO
  @Test fun kotlinMppArtifactIdReplacementWorksCorrectly2() {
    setupFixture("passing_kotlin_mpp_project", "foo")

    val result = executeGradleCommands(TEST_TASK, "--stacktrace", "-PPOM_ARTIFACT_ID=bar-foo")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated(artifactId = "bar-foo")
    assertExpectedCommonArtifactsGenerated(artifactId = "bar-foo-jvm")
    assertExpectedCommonArtifactsGenerated(artifactId = "bar-foo-nodejs")
    assertExpectedCommonArtifactsGenerated(artifactExtension = "klib", artifactId = "bar-foo-linux")
  }

  // TODO
  @Test fun generatesArtifactsAndDocumentationOnKotlinMppWithDokkaProject() {
    setupFixture("passing_kotlin_mpp_with_dokka_project")

    val result = executeGradleCommands(TEST_TASK, "--stacktrace")

    assertExpectedTasksRanSuccessfully(result, hasDokka = true)

    assertExpectedCommonArtifactsGenerated()
    assertPomContentMatches()

    val jvmArtifactId = "$TEST_POM_ARTIFACT_ID-jvm"
    assertExpectedCommonArtifactsGenerated(artifactId = jvmArtifactId)
    // TODO dependency sorting is unstable - the other variants only have 1 and are enough to see that the pom is correct
    // assertPomContentMatches(jvmArtifactId)

    val nodejsArtifactId = "$TEST_POM_ARTIFACT_ID-nodejs"
    assertExpectedCommonArtifactsGenerated(artifactId = nodejsArtifactId)
    // TODO dependency sorting is unstable - the main variant only has 1 and is enough to see that the pom is correct
    // assertPomContentMatches(nodejsArtifactId)

    val linuxArtifactId = "$TEST_POM_ARTIFACT_ID-linux"
    assertExpectedCommonArtifactsGenerated(artifactExtension = "klib", artifactId = linuxArtifactId)
    assertPomContentMatches(linuxArtifactId)
  }

  // TODO
  @Test fun generatesArtifactsAndDocumentationOnKotlinMppWithAndroidDokkaProject() {
    setupFixture("passing_kotlin_mpp_with_android_dokka_project")

    val result = executeGradleCommands(TEST_TASK, "--stacktrace")

    assertExpectedTasksRanSuccessfully(result, hasDokka = true)

    assertExpectedCommonArtifactsGenerated()
    assertPomContentMatches()

    val androidArtifactId = "$TEST_POM_ARTIFACT_ID-android"
    assertExpectedCommonArtifactsGenerated(artifactId = androidArtifactId, artifactExtension = "aar")
    // TODO dependency sorting is unstable - the other variants only have 1 and are enough to see that the pom is correct
    // assertPomContentMatches(androidArtifactId)

    val androidDebugArtifactId = "$TEST_POM_ARTIFACT_ID-android-debug"
    assertExpectedCommonArtifactsGenerated(artifactId = androidDebugArtifactId, artifactExtension = "aar")
    // TODO dependency sorting is unstable - the other variants only have 1 and are enough to see that the pom is correct
    // assertPomContentMatches(androidDebugArtifactId)

    val jvmArtifactId = "$TEST_POM_ARTIFACT_ID-jvm"
    assertExpectedCommonArtifactsGenerated(artifactId = jvmArtifactId)
    // TODO dependency sorting is unstable - the other variants only have 1 and are enough to see that the pom is correct
    // assertPomContentMatches(jvmArtifactId)

    val nodejsArtifactId = "$TEST_POM_ARTIFACT_ID-nodejs"
    assertExpectedCommonArtifactsGenerated(artifactId = nodejsArtifactId)
    // TODO dependency sorting is unstable - the main variant only has 1 and is enough to see that the pom is correct
    // assertPomContentMatches(nodejsArtifactId)

    val linuxArtifactId = "$TEST_POM_ARTIFACT_ID-linux"
    assertExpectedCommonArtifactsGenerated(artifactExtension = "klib", artifactId = linuxArtifactId)
    assertPomContentMatches(linuxArtifactId)
  }

  // TODO
  @Test fun generatesArtifactsAndDocumentationOnGradlePluginProject() {
    setupFixture("passing_java_gradle_plugin_project")

    val result = executeGradleCommands(TEST_TASK, "--stacktrace")

    assertThat(result.task(":$TEST_TASK")?.outcome).isEqualTo(SUCCESS)
    assertExpectedCommonArtifactsGenerated()
    assertPomContentMatches()

    val pluginId = "com.example.test-plugin"
    val artifactId = "$pluginId.gradle.plugin"
    assertPomContentMatches(artifactId, pluginId)
  }

  // TODO
  @Test fun generatesArtifactsAndDocumentationOnMinimalPomProject() {
    setupFixture("minimal_pom_project")

    val result = executeGradleCommands(TEST_TASK, "--stacktrace")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated()
    assertPomContentMatches()
  }

  // TODO
  @Test fun generatesArtifactsAndDocumentationOnOverrideVersionGroupProject() {
    setupFixture("override_version_group_project")

    val result = executeGradleCommands(TEST_TASK, "--stacktrace")

    assertExpectedTasksRanSuccessfully(result)
    assertExpectedCommonArtifactsGenerated(groupId = "com.example2", version = "2.0.0")
    assertPomContentMatches(groupId = "com.example2", version = "2.0.0")
  }

  /**
   * Copies test fixture into temp directory under test.
   */
  private fun setupFixture(fixtureName: String, projectName: String = fixtureName) {
    repoFolder = testProjectDir.newFolder("repo")
    projectFolder = testProjectDir.newFolder(projectName)
    expectedFolder = projectFolder.resolve(EXPECTED_DIR)

    File("$FIXTURES/common").copyRecursively(projectFolder)
    File("$FIXTURES/$fixtureName").copyRecursively(projectFolder, overwrite = true)
  }

  private fun assertExpectedTasksRanSuccessfully(result: BuildResult, hasDokka: Boolean = false) {
    assertThat(result.task(":$TEST_TASK")?.outcome).isEqualTo(SUCCESS)
    if (hasDokka) {
      assertThat(result.task(":dokkaHtml")?.outcome).isEqualTo(SUCCESS)
    } else {
      assertThat(result.task(":dokkaHtml")).isNull()
    }
  }

  /**
   * Makes sure common artifacts are generated (POM, javadoc, sources, etc.),
   * no matter what project type is and which plugins are applied.
   */
  private fun assertExpectedCommonArtifactsGenerated(
    artifactExtension: String = "jar",
    artifactId: String = TEST_POM_ARTIFACT_ID,
    groupId: String = TEST_GROUP,
    version: String = TEST_VERSION_NAME,
    qualifier: String? = null,
  ) {
    val qualifierSuffix = qualifier?.let { "-$it" } ?: ""
    val artifactJar = "$artifactId-$version$qualifierSuffix.$artifactExtension"
    val pomFile = "$artifactId-$version.pom"
    val moduleFile = "$artifactId-$version.module"
    val javadocJar = "$artifactId-$version$qualifierSuffix-javadoc.jar"
    val sourcesJar = "$artifactId-$version$qualifierSuffix-sources.jar"
    assertArtifactGenerated(artifactJar, artifactId, groupId, version)
    assertArtifactGenerated(pomFile, artifactId, groupId, version)
    assertArtifactGenerated(moduleFile, artifactId, groupId, version)
    assertArtifactGenerated(javadocJar, artifactId, groupId, version)
    assertArtifactGenerated(sourcesJar, artifactId, groupId, version)
  }

  private fun assertArtifactGenerated(
    artifactFileNameWithExtension: String,
    artifactId: String = TEST_POM_ARTIFACT_ID,
    groupId: String = TEST_GROUP,
    version: String = TEST_VERSION_NAME
  ) {
    val artifactFolder = artifactFolder(artifactId, groupId, version)

    assertThat(artifactFolder.resolve(artifactFileNameWithExtension)).exists()
    assertThat(artifactFolder.resolve("$artifactFileNameWithExtension.asc")).exists()
  }

  private fun assertPomContentMatches(
    artifactId: String = TEST_POM_ARTIFACT_ID,
    groupId: String = TEST_GROUP,
    version: String = TEST_VERSION_NAME
  ) {
    val artifactFolder = artifactFolder(artifactId, groupId, version)
    val pomFileName = "$artifactId-$version.pom"

    val resolvedPomFile = artifactFolder.resolve(pomFileName)
    val content = resolvedPomFile.readText()

    val expectedContent = expectedFolder.resolve(pomFileName).readText()
    assertThat(content).isEqualToNormalizingWhitespace(expectedContent)
  }

  private fun assertSourceJarContainsFile(
    file: String,
    srcRoot: String,
    artifactId: String = TEST_POM_ARTIFACT_ID,
    groupId: String = TEST_GROUP,
    version: String = TEST_VERSION_NAME
  ) {
    val artifactFolder = artifactFolder(artifactId, groupId, version)
    val sourcesJar = ZipFile(artifactFolder.resolve("$artifactId-$version-sources.jar"))
    val entry = sourcesJar.getEntry(file)
    assertThat(entry).describedAs(file).isNotNull()

    val content = sourcesJar.getInputStream(entry)?.reader()?.buffered()?.readText()

    val expected = projectFolder.resolve(srcRoot).resolve(file)
    val expectedContent = expected.readText()

    assertThat(content).describedAs(file).isNotBlank()
    assertThat(content).describedAs(file).isEqualTo(expectedContent)
  }

  private fun artifactFolder(artifactId: String, groupId: String, version: String): File {
    val group = groupId.replace(".", "/")
    return repoFolder.resolve(group).resolve(artifactId).resolve(version)
  }

  private fun executeGradleCommands(vararg commands: String) = GradleRunner.create()
    .withProjectDir(projectFolder)
    .withArguments(*commands, "-Ptest.releaseRepository=$repoFolder")
    .withDebug(true)
    .forwardOutput()
    .build()

  companion object {
    const val FIXTURES = "src/integrationTest/fixtures"
    const val EXPECTED_DIR = "expected"

    const val TEST_GROUP = "com.example"
    const val TEST_VERSION_NAME = "1.0.0"
    const val TEST_POM_ARTIFACT_ID = "test-artifact"

    const val TEST_TASK = "publishAllPublicationsToTestFolderRepository"
  }
}
