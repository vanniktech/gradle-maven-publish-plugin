package com.vanniktech.maven.publish

import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class MavenPublishPluginIntegrationTest {
    @Rule
    public TemporaryFolder testProjectDir = new TemporaryFolder()
    File repoFolder
    File buildFile
    File gradlePropertiesFile

    @Before
    void setUp() throws Exception {
        repoFolder = testProjectDir.newFolder("repo")

        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'java'
                id 'com.vanniktech.maven.publish'
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
        gradlePropertiesFile = testProjectDir.newFile('gradle.properties')
        gradlePropertiesFile << """
            GROUP=com.example
            VERSION_NAME=1.0.0
            POM_ARTIFACT_ID=test-artifact
        """

        setupFixture("passing_java_project")
    }

    def setupFixture(String fixtureName) {
        AntBuilder antBuilder = new AntBuilder()
        antBuilder.copy(toDir: testProjectDir.root) {
            fileset(dir: "src/integrationTest/fixtures/${fixtureName}")
        }
    }

    @Test
    void generatesArtifactsAndDocumentationOnJavaProject() {
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('javadocsJar', 'sourcesJar', 'installArchives', '--info')
                .withPluginClasspath()
                .build()

        lsGeneratedFiles()

        assert result.task(":installArchives").outcome == SUCCESS

        def group = "com/example"
        def artifactId = "test-artifact"
        def version = "1.0.0"
        def artifactFolder = "$repoFolder.absolutePath/$group/$artifactId/$version"
        def artifactJar = "$artifactId-${version}.jar"
        def javadocJar = "$artifactId-${version}-javadoc.jar"
        def sourcesJar = "$artifactId-${version}-sources.jar"
        assert new File("$artifactFolder/$artifactJar").exists()
        assert new File("$artifactFolder/$javadocJar").exists()
        assert new File("$artifactFolder/$sourcesJar").exists()
    }

    private void lsGeneratedFiles() {
        def proc = "ls -R $repoFolder.absolutePath/com/example/test-artifact".execute()
        def b = new StringBuffer()
        proc.consumeProcessErrorStream(b)
        println proc.text
        println b.toString()
    }
}
