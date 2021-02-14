package com.vanniktech.maven.publish.legacy

import com.vanniktech.maven.publish.baseExtension
import com.vanniktech.maven.publish.findOptionalProperty
import org.gradle.api.Project

internal fun Project.configurePom() {
  // without afterEvaluate https://github.com/gradle/gradle/issues/12259 will happen
  afterEvaluate {
    baseExtension.pom { pom ->
      pom.name.set(project.findOptionalProperty("POM_NAME"))
      pom.description.set(project.findOptionalProperty("POM_DESCRIPTION"))
      pom.url.set(project.findOptionalProperty("POM_URL"))
      pom.inceptionYear.set(project.findOptionalProperty("POM_INCEPTION_YEAR"))

      pom.scm {
        it.url.set(project.findOptionalProperty("POM_SCM_URL"))
        it.connection.set(project.findOptionalProperty("POM_SCM_CONNECTION"))
        it.developerConnection.set(project.findOptionalProperty("POM_SCM_DEV_CONNECTION"))
      }

      pom.licenses { licenses ->
        licenses.license {
          it.name.set(project.findOptionalProperty("POM_LICENCE_NAME"))
          it.url.set(project.findOptionalProperty("POM_LICENCE_URL"))
          it.distribution.set(project.findOptionalProperty("POM_LICENCE_DIST"))
        }
      }

      pom.developers { developers ->
        developers.developer {
          it.id.set(project.findOptionalProperty("POM_DEVELOPER_ID"))
          it.name.set(project.findOptionalProperty("POM_DEVELOPER_NAME"))
          it.url.set(project.findOptionalProperty("POM_DEVELOPER_URL"))
        }
      }
    }
  }
}
