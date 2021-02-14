package com.vanniktech.maven.publish.legacy

import com.vanniktech.maven.publish.baseExtension
import org.gradle.api.Project

internal fun Project.configurePom(publishPom: MavenPublishPom) {
  baseExtension.pom { pom ->
    pom.name.set(publishPom.name)
    pom.description.set(publishPom.description)
    pom.url.set(publishPom.url)
    pom.inceptionYear.set(publishPom.inceptionYear)

    pom.scm {
      it.url.set(publishPom.scmUrl)
      it.connection.set(publishPom.scmConnection)
      it.developerConnection.set(publishPom.scmDeveloperConnection)
    }

    pom.licenses { licenses ->
      licenses.license {
        it.name.set(publishPom.licenseName)
        it.url.set(publishPom.licenseUrl)
        it.distribution.set(publishPom.licenseDistribution)
      }
    }

    pom.developers { developers ->
      developers.developer {
        it.id.set(publishPom.developerId)
        it.name.set(publishPom.developerName)
        it.url.set(publishPom.developerUrl)
      }
    }
  }
}