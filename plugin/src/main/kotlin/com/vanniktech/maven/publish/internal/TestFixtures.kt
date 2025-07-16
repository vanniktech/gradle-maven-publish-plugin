@file:Suppress("InternalGradleApiUsage")

package com.vanniktech.maven.publish.internal

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.DocsType
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.internal.JavaConfigurationVariantMapping
import org.gradle.api.plugins.internal.JavaPluginHelper
import org.gradle.api.plugins.internal.JvmPluginsHelper
import org.gradle.internal.component.external.model.ProjectDerivedCapability
import org.gradle.jvm.component.internal.DefaultJvmSoftwareComponent
import org.gradle.util.GradleVersion

/**
 * Gradle currently doesn't publish a sources jar for test fixtures and the APIs to add
 * one are internal.
 *
 * TODO: https://github.com/gradle/gradle/issues/20539
 */
internal fun addTestFixturesSourcesJar(project: Project) {
  val testFixtureSourceSetName = "testFixtures"
  val extension = project.extensions.getByType(JavaPluginExtension::class.java)
  val testFixturesSourceSet = extension.sourceSets.maybeCreate(testFixtureSourceSetName)
  val projectInternal = project as ProjectInternal

  val projectDerivedCapability = if (GradleVersion.current() >= GradleVersion.version("9.0-milestone-6")) {
    ProjectDerivedCapability::class.java.getConstructor(ProjectInternal::class.java, String::class.java)
  } else {
    ProjectDerivedCapability::class.java.getConstructor(Project::class.java, String::class.java)
  }.newInstance(projectInternal, "testFixtures")

  val sourceElements = if (GradleVersion.current() >= GradleVersion.version("8.6-rc-1")) {
    JvmPluginsHelper.createDocumentationVariantWithArtifact(
      testFixturesSourceSet.sourcesElementsConfigurationName,
      testFixtureSourceSetName,
      DocsType.SOURCES,
      setOf(projectDerivedCapability),
      testFixturesSourceSet.sourcesJarTaskName,
      testFixturesSourceSet.allSource,
      projectInternal,
    )
  } else {
    JvmPluginsHelper::class.java
      .getMethod(
        "createDocumentationVariantWithArtifact",
        String::class.java,
        String::class.java,
        String::class.java,
        List::class.java,
        String::class.java,
        Object::class.java,
        ProjectInternal::class.java,
      ).invoke(
        null,
        testFixturesSourceSet.sourcesElementsConfigurationName,
        testFixtureSourceSetName,
        DocsType.SOURCES,
        listOf(projectDerivedCapability),
        testFixturesSourceSet.sourcesJarTaskName,
        testFixturesSourceSet.allSource,
        projectInternal,
      ) as Configuration
  }

  val component = JavaPluginHelper.getJavaComponent(project) as DefaultJvmSoftwareComponent
  component.addVariantsFromConfiguration(sourceElements, JavaConfigurationVariantMapping("compile", true))
}

/**
 * Gradle will put the project group and version into capabilities instead of using
 * the publication, this can lead to invalid published metadata
 *
 * TODO: https://github.com/gradle/gradle/issues/23354
 */
internal fun fixTestFixturesMetadata(project: Project) {
  project.afterEvaluate {
    project.group = project.baseExtension.groupId.get()
    project.version = project.baseExtension.version.get()
  }
}
