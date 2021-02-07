package com.vanniktech.maven.publish

import org.gradle.api.Project

internal data class MavenPublishPom(
  val groupId: String,
  val artifactId: String,
  val version: String,

  val name: String?,
  val packaging: String?,
  val description: String?,
  val url: String?,
  val inceptionYear: String?,

  val scmUrl: String?,
  val scmConnection: String?,
  val scmDeveloperConnection: String?,

  val licenseName: String?,
  val licenseUrl: String?,
  val licenseDistribution: String?,

  val developerId: String?,
  val developerName: String?,
  val developerUrl: String?
) {

  internal companion object {
    @JvmStatic
    fun fromProject(project: Project) = MavenPublishPom(
        project.findOptionalProperty("GROUP") ?: project.group.toString(),
        project.findOptionalProperty("POM_ARTIFACT_ID") ?: project.name,
        project.findOptionalProperty("VERSION_NAME") ?: project.version.toString(),
        project.findOptionalProperty("POM_NAME"),
        project.findOptionalProperty("POM_PACKAGING"),
        project.findOptionalProperty("POM_DESCRIPTION"),
        project.findOptionalProperty("POM_URL"),
        project.findOptionalProperty("POM_INCEPTION_YEAR"),
        project.findOptionalProperty("POM_SCM_URL"),
        project.findOptionalProperty("POM_SCM_CONNECTION"),
        project.findOptionalProperty("POM_SCM_DEV_CONNECTION"),
        project.findOptionalProperty("POM_LICENCE_NAME"),
        project.findOptionalProperty("POM_LICENCE_URL"),
        project.findOptionalProperty("POM_LICENCE_DIST"),
        project.findOptionalProperty("POM_DEVELOPER_ID"),
        project.findOptionalProperty("POM_DEVELOPER_NAME"),
        project.findOptionalProperty("POM_DEVELOPER_URL")
    )
  }
}
