package com.vanniktech.maven.publish

import org.gradle.api.Project

internal data class MavenPublishPom(
  val groupId: String,
  val artifactId: String,
  val version: String,

  val name: String,
  val packaging: String,
  val description: String?,
  val url: String,

  val scmUrl: String,
  val scmConnection: String,
  val scmDeveloperConnection: String,

  val licenseName: String,
  val licenseUrl: String,
  val licenseDistribution: String,

  val developerId: String,
  val developerName: String
) {

  internal companion object {
    @JvmStatic
    fun fromProject(project: Project) = MavenPublishPom(
        findMandatoryProperty(project, "GROUP"),
        findMandatoryProperty(project, "POM_ARTIFACT_ID"),
        findMandatoryProperty(project, "VERSION_NAME"),
        findMandatoryProperty(project, "POM_NAME"),
        findMandatoryProperty(project, "POM_PACKAGING"),
        findOptionalProperty(project, "POM_DESCRIPTION"),
        findMandatoryProperty(project, "POM_URL"),
        findMandatoryProperty(project, "POM_SCM_URL"),
        findMandatoryProperty(project, "POM_SCM_CONNECTION"),
        findMandatoryProperty(project, "POM_SCM_DEV_CONNECTION"),
        findMandatoryProperty(project, "POM_LICENCE_NAME"),
        findMandatoryProperty(project, "POM_LICENCE_URL"),
        findMandatoryProperty(project, "POM_LICENCE_DIST"),
        findMandatoryProperty(project, "POM_DEVELOPER_ID"),
        findMandatoryProperty(project, "POM_DEVELOPER_NAME")
    )

    private fun findMandatoryProperty(project: Project, propertyName: String): String {
      val value = project.findProperty(propertyName) as String?
      if (value == null) {
        throw IllegalArgumentException("please define $propertyName in your gradle.properties")
      } else {
        return value
      }
    }
    private fun findOptionalProperty(project: Project, propertyName: String) = project.findProperty(propertyName) as String?
  }
}
