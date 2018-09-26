package com.vanniktech.maven.publish

import org.assertj.core.api.Java6Assertions.assertThat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.EnvironmentVariables

class MavenPublishPluginExtensionTest {
  @get:Rule val environmentVariables = EnvironmentVariables()

  private lateinit var project: Project

  @Before fun setUp() {
    project = ProjectBuilder.builder().withName("project").build()
  }

  @Test fun defaultReleaseRepositoryUrl() {
    assertThat(MavenPublishPluginExtension(project).uploadArchives.releaseRepositoryUrl).isEqualTo("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
  }

  @Test fun defaultSnapshotRepositoryUrl() {
    assertThat(MavenPublishPluginExtension(project).uploadArchives.snapshotRepositoryUrl).isEqualTo("https://oss.sonatype.org/content/repositories/snapshots/")
  }

  @Test fun defaultReleaseRepositoryUrlEnvironmentVariable() {
    environmentVariables.set("RELEASE_REPOSITORY_URL", "https://releases.fake/")
    assertThat(MavenPublishPluginExtension(project).uploadArchives.releaseRepositoryUrl).isEqualTo("https://releases.fake/")
  }

  @Test fun defaultSnapshotRepositoryUrlEnvironmentVariable() {
    environmentVariables.set("SNAPSHOT_REPOSITORY_URL", "https://snapshots.fake/")
    assertThat(MavenPublishPluginExtension(project).uploadArchives.snapshotRepositoryUrl).isEqualTo("https://snapshots.fake/")
  }

  @Test fun defaultRepositoryUsernameEnvironmentVariable() {
    environmentVariables.set("SONATYPE_NEXUS_USERNAME", "env")
    assertThat(MavenPublishPluginExtension(project).uploadArchives.repositoryUsername).isEqualTo("env")
  }

  @Test fun defaultRepositoryUsernameNothing() {
    assumeTrue(System.getenv("TRAVIS") != "true") // Do not run on travis since we inject the same values.
    assertThat(MavenPublishPluginExtension(project).uploadArchives.repositoryUsername).isNull()
  }

  @Test fun defaultRepositoryPasswordEnvironmentVariable() {
    environmentVariables.set("SONATYPE_NEXUS_PASSWORD", "env")
    assertThat(MavenPublishPluginExtension(project).uploadArchives.repositoryPassword).isEqualTo("env")
  }

  @Test fun defaultRepositoryPasswordNothing() {
    assumeTrue(System.getenv("TRAVIS") != "true") // Do not run on travis since we inject the same values.
    assertThat(MavenPublishPluginExtension(project).uploadArchives.repositoryPassword).isNull()
  }

  private val MavenPublishPluginExtension.uploadArchives get() = targets.getByName("uploadArchives")
}
