package com.vanniktech.maven.publish

import org.gradle.api.Project

internal data class MavenPublishPom(
  val groupId: String?,
  val artifactId: String?,
  val version: String?,

  val name: String?,
  val packaging: String?,
  val description: String?,
  val url: String?,

  val scmUrl: String?,
  val scmConnection: String?,
  val scmDeveloperConnection: String?,

  val licenseName: String?,
  val licenseUrl: String?,
  val licenseDistribution: String?,

  val developerId: String?,
  val developerName: String?
) {

  internal companion object {
    @JvmStatic
    fun fromProject(project: Project) = MavenPublishPom(
        project.findProperty("GROUP") as String?,
        project.findProperty("POM_ARTIFACT_ID") as String?,
        project.findProperty("VERSION_NAME") as String?,
        project.findProperty("POM_NAME") as String?,
        project.findProperty("POM_PACKAGING") as String?,
        project.findProperty("POM_DESCRIPTION") as String?,
        project.findProperty("POM_URL") as String?,
        project.findProperty("POM_SCM_URL") as String?,
        project.findProperty("POM_SCM_CONNECTION") as String?,
        project.findProperty("POM_SCM_DEV_CONNECTION") as String?,
        project.findProperty("POM_LICENCE_NAME") as String?,
        project.findProperty("POM_LICENCE_URL") as String?,
        project.findProperty("POM_LICENCE_DIST") as String?,
        project.findProperty("POM_DEVELOPER_ID") as String?,
        project.findProperty("POM_DEVELOPER_NAME") as String?
    )
  }
}
