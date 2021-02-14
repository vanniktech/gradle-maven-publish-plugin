package com.vanniktech.maven.publish

import org.gradle.api.Action
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication

@Incubating
open class MavenPublishBaseExtension(
  private val project: Project
) {

  /**
   * Configures the POM that will be published.
   *
   * See the [Gradle publishing guide](https://docs.gradle.org/current/userguide/publishing_maven.html#sec:modifying_the_generated_pom)
   * for how to use it.
   */
  @Incubating
  fun pom(configure: Action<in MavenPom>) {
    project.gradlePublishing.publications.withType(MavenPublication::class.java).configureEach {
      it.pom(configure)
    }
  }
}
