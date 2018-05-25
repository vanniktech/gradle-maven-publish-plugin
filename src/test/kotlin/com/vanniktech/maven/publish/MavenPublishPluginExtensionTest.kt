package com.vanniktech.maven.publish

import org.assertj.core.api.Java6Assertions.assertThat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
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
    assertThat(MavenPublishPluginExtension(project).releaseRepositoryUrl).isEqualTo("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
  }

  @Test fun defaultSnapshotRepositoryUrl() {
    assertThat(MavenPublishPluginExtension(project).snapshotRepositoryUrl).isEqualTo("https://oss.sonatype.org/content/repositories/snapshots/")
  }

  @Test fun defaultRepositoryUsernameEnvironmentVariable() {
    environmentVariables.set("SONATYPE_NEXUS_USERNAME", "env")
    assertThat(MavenPublishPluginExtension(project).repositoryUsername).isEqualTo("env")
  }

  @Test fun defaultRepositoryUsernameNothing() {
    assertThat(MavenPublishPluginExtension(project).repositoryUsername).isNull()
  }

  @Test fun defaultRepositoryPasswordEnvironmentVariable() {
    environmentVariables.set("SONATYPE_NEXUS_PASSWORD", "env")
    assertThat(MavenPublishPluginExtension(project).repositoryPassword).isEqualTo("env")
  }

  @Test fun defaultRepositoryPasswordNothing() {
    assertThat(MavenPublishPluginExtension(project).repositoryPassword).isNull()
  }
}
