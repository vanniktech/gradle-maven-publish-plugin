@file:Suppress("InternalGradleApiUsage")

package com.vanniktech.maven.publish.workaround

import com.vanniktech.maven.publish.baseExtension
import org.gradle.api.Project
import org.gradle.api.attributes.DocsType
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.internal.JavaConfigurationVariantMapping
import org.gradle.api.plugins.internal.JvmPluginsHelper
import org.gradle.api.provider.Provider
import org.gradle.internal.component.external.model.ProjectDerivedCapability

@Suppress("GradleProjectIsolation") // TODO: https://github.com/gradle/gradle/issues/23572
internal fun Project.gradlePropertyCompat(propertyName: String): Provider<String> = providers
  .gradleProperty(propertyName)
  .orElse(provider { findProperty(propertyName)?.toString() })

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
  val projectDerivedCapability = ProjectDerivedCapability(projectInternal, "testFixtures")
  val sourceElements = JvmPluginsHelper.createDocumentationVariantWithArtifact(
    testFixturesSourceSet.sourcesElementsConfigurationName,
    testFixtureSourceSetName,
    DocsType.SOURCES,
    setOf(projectDerivedCapability),
    testFixturesSourceSet.sourcesJarTaskName,
    testFixturesSourceSet.allSource,
    projectInternal,
  )
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
