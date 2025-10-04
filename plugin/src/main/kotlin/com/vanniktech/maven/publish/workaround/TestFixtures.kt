@file:Suppress("InternalGradleApiUsage")

package com.vanniktech.maven.publish.workaround

import com.vanniktech.maven.publish.baseExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.DocsType
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.internal.JavaConfigurationVariantMapping
import org.gradle.api.plugins.internal.JvmPluginsHelper
import org.gradle.api.provider.Provider
import org.gradle.internal.component.external.model.ProjectDerivedCapability
import org.gradle.util.GradleVersion

/**
 * Gradle currently doesn't publish a sources jar for test fixtures and the APIs to add
 * one are internal.
 *
 * https://github.com/gradle/gradle/issues/20539
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

  // use reflection because return type changed in Gradle 9.2.0-milestone-1
  val sourceElements = JvmPluginsHelper.createDocumentationVariantWithArtifact(
    testFixturesSourceSet.sourcesElementsConfigurationName,
    testFixtureSourceSetName,
    DocsType.SOURCES,
    setOf(projectDerivedCapability),
    testFixturesSourceSet.sourcesJarTaskName,
    testFixturesSourceSet.allSource,
    projectInternal,
  )

  // the following is not using private APIs just needs to adapt to the API change in the method above
  val component = project.components.findByName("java") as AdhocComponentWithVariants
  component.addVariantsFromConfiguration(sourceElements, JavaConfigurationVariantMapping("compile", true))
}

/**
 * Gradle will put the project group and version into capabilities instead of using
 * the publication, this can lead to invalid published metadata
 *
 * https://github.com/gradle/gradle/issues/23354
 */
internal fun fixTestFixturesMetadata(project: Project) {
  project.afterEvaluate {
    project.group = project.baseExtension.groupId.get()
    project.version = project.baseExtension.version.get()
  }
}
