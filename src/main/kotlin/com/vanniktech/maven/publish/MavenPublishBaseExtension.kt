package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.nexus.CloseAndReleaseRepositoryTask
import com.vanniktech.maven.publish.nexus.NexusOptions
import org.gradle.api.Action
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication

@Incubating
@Suppress("UnnecessaryAbstractClass")
abstract class MavenPublishBaseExtension(
  private val project: Project
) {

  private var nexusOptions: NexusOptions? = null

  /**
   * Sets up Maven Central publishing through Sonatype OSSRH by configuring the target repository. Gradle will then
   * automatically create a `publishAllPublicationsToMavenRepository` task as well as include it in the general
   * `publish` task. If the current version ends with `-SNAPSHOT` the artifacts will be published to Sonatype's snapshot
   * repository instead.
   *
   * This expects you provide your Sonatype user name and password through Gradle properties called
   * `mavenCentralRepositoryUsername` and `mavenCentralRepositoryPassword`.
   *
   * The `closeAndReleaseRepository` task is automatically configured for Sonatype OSSRH using the same credentials.
   */
  @Incubating
  fun publishToMavenCentral() {
    project.gradlePublishing.repositories.maven { repo ->
      repo.name = "mavenCentral"
      repo.setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
      repo.credentials(PasswordCredentials::class.java)

      project.afterEvaluate {
        if (it.version.toString().endsWith("SNAPSHOT")) {
          repo.setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
        }
      }
    }

    nexusOptions {
      it.baseUrl = "https://oss.sonatype.org/service/local/"
      it.repositoryUsername = project.findOptionalProperty("mavenCentralRepositoryUsername")
      it.repositoryPassword = project.findOptionalProperty("mavenCentralRepositoryPassword")
    }
  }

  internal fun nexusOptions(action: Action<NexusOptions>) {
    var nexusOptions = this.nexusOptions
    if (nexusOptions == null) {
      nexusOptions = NexusOptions(null, null, null, null)
      this.nexusOptions = nexusOptions

      @Suppress("SwallowedException")
      try {
        project.rootProject.tasks.named("closeAndReleaseRepository")
      } catch (e: UnknownTaskException) {
        project.rootProject.tasks.register("closeAndReleaseRepository", CloseAndReleaseRepositoryTask::class.java) {
          it.description = "Closes and releases an artifacts repository in Nexus"
          it.group = "release"
          it.nexusOptions = nexusOptions
        }
      }
    }

    action.execute(nexusOptions)
  }

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
