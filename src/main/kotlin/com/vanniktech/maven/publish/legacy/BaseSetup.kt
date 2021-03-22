package com.vanniktech.maven.publish.legacy

import com.vanniktech.maven.publish.baseExtension
import com.vanniktech.maven.publish.findOptionalProperty
import com.vanniktech.maven.publish.legacyExtension
import org.gradle.api.Project

internal fun Project.configureSigning() {
  afterEvaluate {
    if (legacyExtension.releaseSigningEnabled) {
      baseExtension.signAllPublications()
    }
  }
}

internal fun Project.configurePom() {
  // without afterEvaluate https://github.com/gradle/gradle/issues/12259 will happen
  afterEvaluate {
    baseExtension.pom { pom ->
      val name = project.findOptionalProperty("POM_NAME")
      if (name != null) {
        pom.name.set(name)
      }
      val description = project.findOptionalProperty("POM_DESCRIPTION")
      if (description != null) {
        pom.description.set(description)
      }
      val url = project.findOptionalProperty("POM_URL")
      if (url != null) {
        pom.url.set(url)
      }
      val inceptionYear = project.findOptionalProperty("POM_INCEPTION_YEAR")
      if (inceptionYear != null) {
        pom.inceptionYear.set(inceptionYear)
      }

      val scmUrl = project.findOptionalProperty("POM_SCM_URL")
      val scmConnection = project.findOptionalProperty("POM_SCM_CONNECTION")
      val scmDeveloperConnection = project.findOptionalProperty("POM_SCM_DEV_CONNECTION")
      if (scmUrl != null || scmConnection != null || scmDeveloperConnection != null) {
        pom.scm {
          it.url.set(scmUrl)
          it.connection.set(scmConnection)
          it.developerConnection.set(scmDeveloperConnection)
        }
      }

      val licenseName = project.findOptionalProperty("POM_LICENCE_NAME")
      val licenseUrl = project.findOptionalProperty("POM_LICENCE_URL")
      val licenseDistribution = project.findOptionalProperty("POM_LICENCE_DIST")
      if (licenseName != null || licenseUrl != null || licenseDistribution != null) {
        pom.licenses { licenses ->
          licenses.license {
            it.name.set(licenseName)
            it.url.set(licenseUrl)
            it.distribution.set(licenseDistribution)
          }
        }
      }

      val developerId = project.findOptionalProperty("POM_DEVELOPER_ID")
      val developerName = project.findOptionalProperty("POM_DEVELOPER_NAME")
      val developerUrl = project.findOptionalProperty("POM_DEVELOPER_URL")
      if (developerId != null || developerName != null || developerUrl != null) {
        pom.developers { developers ->
          developers.developer {
            it.id.set(developerId)
            it.name.set(developerName)
            it.url.set(developerUrl)
          }
        }
      }
    }
  }
}
