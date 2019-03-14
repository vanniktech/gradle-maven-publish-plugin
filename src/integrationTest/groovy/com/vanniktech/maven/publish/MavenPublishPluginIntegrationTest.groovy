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
  String artifactFolder

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

    def group = TEST_GROUP.replace(".", "/")
    def artifactId = TEST_POM_ARTIFACT_ID
    def version = TEST_VERSION_NAME
    artifactFolder = "$repoFolder.absolutePath/$group/$artifactId/$version"
  }

  @Test
  void generatesArtifactsAndDocumentationOnJavaProject() {
    buildFile << """
      apply plugin: "java"
    """
    setupFixture("passing_java_project")

    def result = executeGradleCommands(
        'javadocsJar',
        'sourcesJar',
        'installArchives',
        '--info'
    )

    assert result.task(":installArchives").outcome == SUCCESS
    assertExpectedCommonArtifactsGenerated()
  }

  @Test
  void generatesArtifactsAndDocumentationOnJavaLibraryProject() {
    buildFile << """
      apply plugin: "java-library"
    """
    setupFixture("passing_java_library_project")

    def result = executeGradleCommands(
        'javadocsJar',
        'sourcesJar',
        'installArchives',
        '--info'
    )

    assert result.task(":installArchives").outcome == SUCCESS
    assertExpectedCommonArtifactsGenerated()
  }

  @Test
  void generatesArtifactsAndDocumentationOnJavaLibraryWithGroovyProject() {
    buildFile << """
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
    """
    setupFixture("passing_java_library_with_groovy_project")

    def result = executeGradleCommands(
        'javadocsJar',
        'groovydocJar',
        'sourcesJar',
        'installArchives',
        '--info'
    )

    assert result.task(":installArchives").outcome == SUCCESS
    assertExpectedCommonArtifactsGenerated()
    assertArtifactGenerated("$TEST_POM_ARTIFACT_ID-${TEST_VERSION_NAME}-groovydoc.jar")
  }

  private void assertExpectedCommonArtifactsGenerated() {
    def artifactJar = "$TEST_POM_ARTIFACT_ID-${TEST_VERSION_NAME}.jar"
    def pomFile = "$TEST_POM_ARTIFACT_ID-${TEST_VERSION_NAME}.pom"
    def javadocJar = "$TEST_POM_ARTIFACT_ID-${TEST_VERSION_NAME}-javadoc.jar"
    def sourcesJar = "$TEST_POM_ARTIFACT_ID-${TEST_VERSION_NAME}-sources.jar"
    assertArtifactGenerated(artifactJar)
    assertArtifactGenerated(pomFile)
    assertArtifactGenerated(javadocJar)
    assertArtifactGenerated(sourcesJar)
  }

  private void assertArtifactGenerated(String artifactFileNameWithExtenstion) {
    assert new File("$artifactFolder/$artifactFileNameWithExtenstion").exists()
  }

  private def executeGradleCommands(String... commands) {
    return GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments(commands)
        .withPluginClasspath()
        .build()
  }
}
