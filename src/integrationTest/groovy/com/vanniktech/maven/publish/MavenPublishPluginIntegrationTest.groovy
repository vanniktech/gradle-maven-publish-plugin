package com.vanniktech.maven.publish

import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class MavenPublishPluginIntegrationTest {
  static final String TEST_GROUP = "com.example"
  static final String TEST_VERSION_NAME = "1.0.0"
  static final String TEST_POM_ARTIFACT_ID = "test-artifact"

  @Rule
  public TemporaryFolder testProjectDir = new TemporaryFolder()
  File repoFolder
  File buildFile

  /**
   * Copies test fixture into temp directory under test.
   */
  def setupFixture(String fixtureName) {
    AntBuilder antBuilder = new AntBuilder()
    antBuilder.copy(toDir: testProjectDir.root) {
      fileset(dir: "src/integrationTest/fixtures/${fixtureName}")
    }
  }

  @Before
  void setUp() throws Exception {
    repoFolder = testProjectDir.newFolder("repo")
    buildFile = testProjectDir.newFile('build.gradle')
    buildFile << """
      plugins {
        id "com.vanniktech.maven.publish"
      }
      mavenPublish {
        targets {
          installArchives {
            releaseRepositoryUrl = "file://${repoFolder.absolutePath}"
            signing = false
          }
        }
      }
    """
    testProjectDir.newFile('gradle.properties') << """
      GROUP=$TEST_GROUP
      VERSION_NAME=$TEST_VERSION_NAME
      POM_ARTIFACT_ID=$TEST_POM_ARTIFACT_ID
    """
  }

  @Test
  void generatesArtifactsAndDocumentationOnJavaProject() {
    buildFile << """
      apply plugin: "java"
    """
    setupFixture("passing_java_project")

    def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments('javadocsJar', 'sourcesJar', 'installArchives', '--info')
        .withPluginClasspath()
        .build()

    assert result.task(":installArchives").outcome == SUCCESS
    assertExpectedArtifactsGenerated()
  }

  private void assertExpectedArtifactsGenerated() {
    def group = "com/example"
    def artifactId = "test-artifact"
    def version = "1.0.0"
    def artifactFolder = "$repoFolder.absolutePath/$group/$artifactId/$version"
    def artifactJar = "$artifactId-${version}.jar"
    def pomFile = "$artifactId-${version}.pom"
    def javadocJar = "$artifactId-${version}-javadoc.jar"
    def sourcesJar = "$artifactId-${version}-sources.jar"
    assert new File("$artifactFolder/$artifactJar").exists()
    assert new File("$artifactFolder/$pomFile").exists()
    assert new File("$artifactFolder/$javadocJar").exists()
    assert new File("$artifactFolder/$sourcesJar").exists()
  }
}
