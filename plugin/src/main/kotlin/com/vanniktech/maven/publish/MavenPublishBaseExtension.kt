package com.vanniktech.maven.publish

import java.util.concurrent.Callable
import org.gradle.api.Action
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningPlugin

@Incubating
abstract class MavenPublishBaseExtension(
  private val project: Project
) {

  private val sonatypeHost: Property<SonatypeHost> = project.objects.property(SonatypeHost::class.java)
  private val signing: Property<Boolean> = project.objects.property(Boolean::class.java)
  private val pomFromProperties: Property<Boolean> = project.objects.property(Boolean::class.java)
  private val platform: Property<Platform> = project.objects.property(Platform::class.java)

  // will be set by the task to create the staging repository
  private val stagingRepositoryId: Property<String> = project.objects.property(String::class.java).apply {
    finalizeValueOnRead()
  }

  /**
   * Sets up Maven Central publishing through Sonatype OSSRH by configuring the target repository. Gradle will then
   * automatically create a `publishAllPublicationsToMavenRepository` task as well as include it in the general
   * `publish` task. If the current version ends with `-SNAPSHOT` the artifacts will be published to Sonatype's snapshot
   * repository instead.
   *
   * This expects you provide your Sonatype user name and password through Gradle properties called
   * `mavenCentralUsername` and `mavenCentralPassword`.
   *
   * The `closeAndReleaseRepository` task is automatically configured for Sonatype OSSRH using the same credentials.
   *
   * @param host the instance of Sonatype OSSRH to use
   */
  fun publishToMavenCentral(host: SonatypeHost = SonatypeHost.DEFAULT) {
    sonatypeHost.set(host)
    sonatypeHost.finalizeValue()

    project.gradlePublishing.repositories.maven { repo ->
      repo.name = "mavenCentral"
      repo.setUrl("${host.rootUrl}/service/local/staging/deploy/maven2/")
      repo.credentials(PasswordCredentials::class.java)

      project.afterEvaluate {
        if (it.version.toString().endsWith("SNAPSHOT")) {
          repo.setUrl("${host.rootUrl}/content/repositories/snapshots/")
        }
      }
    }

    project.rootExtension.configureCloseAndReleaseTask(
      baseUrl = "${host.rootUrl}/service/local/",
      repositoryUsername = project.findOptionalProperty("mavenCentralUsername"),
      repositoryPassword = project.findOptionalProperty("mavenCentralPassword")
    )
  }

  /**
   * Automatically apply Gradle's `signing` plugin and configure all publications to be signed. If signing credentials
   * are not configured this will fail the build unless the current version is a `SNAPSHOT`.
   *
   * Signing can be done using a local `secring.gpg` by setting these Gradle properties:
   * ```
   * signing.keyId=24875D73
   * signing.password=secret
   * signing.secretKeyRingFile=/Users/me/.gnupg/secring.gpg
   * ```
   *
   * Alternatively an in memory key can be used by exporting an ascii-armored GPG key and setting these Gradle properties"
   * ```
   * signingInMemoryKey=exported_ascii_armored_key
   * # optional
   * signingInMemoryKeyId=24875D73
   * # if key was created with a password
   * signingInMemoryKeyPassword=secret
   * ```
   * `gpg2 --export-secret-keys --armor KEY_ID` can be used to export they key for this. The exported key is taken
   * without the first line and without the last 2 lines, all line breaks should be removed as well. The in memory
   * properties can also be provided as environment variables by prefixing them with `ORG_GRADLE_PROJECT_`, e.g.
   * `ORG_GRADLE_PROJECT_signingInMemoryKey`.
   *
   * More information about signing as well as different ways to provide credentials
   * can be found in the [Gradle documentation](https://docs.gradle.org/current/userguide/signing_plugin.html)
   */
  // TODO update in memory set up once https://github.com/gradle/gradle/issues/16056 is implemented
  fun signAllPublications() {
    signing.set(true)
    signing.finalizeValue()

    project.plugins.apply(SigningPlugin::class.java)
    project.gradleSigning.setRequired(Callable { !project.version.toString().contains("SNAPSHOT") })
    project.gradleSigning.sign(project.gradlePublishing.publications)

    val inMemoryKey = project.findOptionalProperty("signingInMemoryKey")
    if (inMemoryKey != null) {
      val inMemoryKeyId = project.findOptionalProperty("signingInMemoryKeyId")
      val inMemoryKeyPassword = project.findOptionalProperty("signingInMemoryKeyPassword") ?: ""
      project.gradleSigning.useInMemoryPgpKeys(inMemoryKeyId, inMemoryKey, inMemoryKeyPassword)
    }
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

  /**
   * Configures the POM through Gradle properties.
   */
  @Incubating
  fun pomFromGradleProperties() {
    pomFromProperties.set(true)
    pomFromProperties.finalizeValue()

    // without afterEvaluate https://github.com/gradle/gradle/issues/12259 will happen
    project.afterEvaluate {
      pom { pom ->
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

        val issueManagementSystem = project.findOptionalProperty("POM_ISSUE_SYSTEM")
        val issueManagementUrl = project.findOptionalProperty("POM_ISSUE_URL")
        if (issueManagementSystem != null || issueManagementUrl != null) {
          pom.issueManagement {
            it.system.set(issueManagementSystem)
            it.url.set(issueManagementUrl)
          }
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

        val licenceName = project.findOptionalProperty("POM_LICENCE_NAME")
        val licenceUrl = project.findOptionalProperty("POM_LICENCE_URL")
        val licenceDistribution = project.findOptionalProperty("POM_LICENCE_DIST")
        if (licenceName != null || licenceUrl != null || licenceDistribution != null) {
          pom.licenses { licences ->
            licences.license {
              it.name.set(licenceName)
              it.url.set(licenceUrl)
              it.distribution.set(licenceDistribution)
            }
          }
        }

        val licenseName = project.findOptionalProperty("POM_LICENSE_NAME")
        val licenseUrl = project.findOptionalProperty("POM_LICENSE_URL")
        val licenseDistribution = project.findOptionalProperty("POM_LICENSE_DIST")
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
        val developerEmail = project.findOptionalProperty("POM_DEVELOPER_EMAIL")
        if (developerId != null || developerName != null || developerUrl != null) {
          pom.developers { developers ->
            developers.developer {
              it.id.set(developerId)
              it.name.set(developerName)
              it.url.set(developerUrl)
              it.email.set(developerEmail)
            }
          }
        }
      }
    }
  }

  /**
   * Configures a [Platform] which will automatically set up the artifacts that should get published, including javadoc
   * and sources jars depending on the option.
   */
  @Incubating
  fun configure(platform: Platform) {
    this.platform.set(platform)
    this.platform.finalizeValue()

    platform.configure(project)
  }
}
