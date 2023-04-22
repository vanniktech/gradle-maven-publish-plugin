package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.sonatype.CloseAndReleaseSonatypeRepositoryTask.Companion.registerCloseAndReleaseRepository
import com.vanniktech.maven.publish.sonatype.CreateSonatypeRepositoryTask.Companion.registerCreateRepository
import com.vanniktech.maven.publish.sonatype.DropSonatypeRepositoryTask.Companion.registerDropRepository
import com.vanniktech.maven.publish.sonatype.SonatypeRepositoryBuildService.Companion.registerSonatypeRepositoryBuildService
import org.gradle.api.Action
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningPlugin
import org.gradle.util.GradleVersion

abstract class MavenPublishBaseExtension(
  private val project: Project,
) {

  private val sonatypeHost: Property<SonatypeHost> = project.objects.property(SonatypeHost::class.java)
  private val signing: Property<Boolean> = project.objects.property(Boolean::class.java)
  internal val groupId: Property<String> = project.objects.property(String::class.java)
    .convention(project.provider { project.group.toString() })
  internal val version: Property<String> = project.objects.property(String::class.java)
    .convention(project.provider { project.version.toString() })
  private val pomFromProperties: Property<Boolean> = project.objects.property(Boolean::class.java)
  private val platform: Property<Platform> = project.objects.property(Platform::class.java)

  /**
   * Sets up Maven Central publishing through Sonatype OSSRH by configuring the target repository. Gradle will then
   * automatically create a `publishAllPublicationsToMavenCentralRepository` task as well as include it in the general
   * `publish` task. As part of running publish the plugin will automatically create a staging repostory on Sonatype
   * to which all artifacts will be published. At the end of the build this staging repository will be automatically
   * closed. When the [automaticRelease] parameter is `true` the staging repository will also be released
   * automatically afterwards.
   * If the current version ends with `-SNAPSHOT` the artifacts will be published to Sonatype's snapshot
   * repository instead.
   *
   * This expects you provide your Sonatype username and password through Gradle properties called
   * `mavenCentralUsername` and `mavenCentralPassword`.
   *
   * The `closeAndReleaseRepository` task is automatically configured for Sonatype OSSRH using the same credentials.
   *
   * @param host the instance of Sonatype OSSRH to use
   * @param automaticRelease whether a non SNAPSHOT build should be released automatically at the end of the build
   */
  @JvmOverloads
  fun publishToMavenCentral(host: SonatypeHost = SonatypeHost.DEFAULT, automaticRelease: Boolean = false) {
    sonatypeHost.set(host)
    sonatypeHost.finalizeValue()

    val usingPlainConsole = project.isUsingPlainConsole
    val buildService = project.registerSonatypeRepositoryBuildService(
      sonatypeHost = sonatypeHost,
      repositoryUsername = project.providers.gradleProperty("mavenCentralUsername"),
      repositoryPassword = project.providers.gradleProperty("mavenCentralPassword"),
      automaticRelease = automaticRelease,
    )

    val versionIsSnapshot = version.map { it.endsWith("-SNAPSHOT") }
    val createRepository = project.tasks.registerCreateRepository(groupId, versionIsSnapshot, usingPlainConsole, buildService)
    val stagingRepositoryId = createRepository.flatMap { it.stagingRepositoryId }

    project.gradlePublishing.repositories.maven { repo ->
      repo.name = "mavenCentral"
      repo.setUrl(sonatypeHost.map { it.publishingUrl(versionIsSnapshot.get(), stagingRepositoryId) })
      repo.credentials(PasswordCredentials::class.java)
    }

    project.tasks.withType(PublishToMavenRepository::class.java).configureEach { publishTask ->
      if (publishTask.name.endsWith("ToMavenCentralRepository")) {
        publishTask.dependsOn(createRepository)
      }
    }

    project.tasks.registerCloseAndReleaseRepository(usingPlainConsole, buildService)
    project.tasks.registerDropRepository(buildService)
  }

  @JvmOverloads
  @JvmSynthetic
  fun publishToMavenCentral(host: String, automaticRelease: Boolean = false) {
    publishToMavenCentral(SonatypeHost.valueOf(host), automaticRelease)
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
    project.gradleSigning.setRequired(version.map { !it.endsWith("-SNAPSHOT") })

    val inMemoryKey = project.findOptionalProperty("signingInMemoryKey")
    if (inMemoryKey != null) {
      val inMemoryKeyId = project.findOptionalProperty("signingInMemoryKeyId")
      val inMemoryKeyPassword = project.findOptionalProperty("signingInMemoryKeyPassword") ?: ""
      project.gradleSigning.useInMemoryPgpKeys(inMemoryKeyId, inMemoryKey, inMemoryKeyPassword)
    }

    project.mavenPublications { publication ->
      if (GradleVersion.current() < GradleVersion.version("8.0-rc-1")) {
        // workaround incompatibility with other plugins because sign(publication) was not idempotent
        val task = project.tasks.findByName("sign${publication.name.capitalize()}Publication")
        if (task == null) {
          project.gradleSigning.sign(publication)
        }
      } else {
        project.gradleSigning.sign(publication)
      }
    }

    // TODO: remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
    project.tasks.withType(AbstractPublishToMaven::class.java).configureEach { publishTask ->
      publishTask.dependsOn(project.tasks.withType(Sign::class.java))
    }
  }

  /**
   * Set the Maven coordinates consisting of [groupId], [artifactId] and [version] for this project. In the case of
   * Kotlin Multiplatform projects the given [artifactId] is used together with the platform targets resulting in
   * artifactIds like `[artifactId]-jvm`.
   */
  @Incubating
  fun coordinates(groupId: String, artifactId: String, version: String) {
    groupId(groupId)
    artifactId(artifactId)
    version(version)
  }

  private fun groupId(groupId: String) {
    this.groupId.set(groupId)
    this.groupId.finalizeValueOnRead()

    // skip the plugin marker artifact which has its own group id based on the plugin id
    project.mavenPublicationsWithoutPluginMarker {
      it.groupId = this.groupId.get()
    }
  }

  private fun artifactId(artifactId: String) {
    // skip the plugin marker artifact which has its own artifact id based on the plugin id
    project.mavenPublicationsWithoutPluginMarker {
      // the multiplatform plugin creates its own publications, so it is ok to use hasPlugin in here
      if (project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
        // needed to avoid the mpp plugin writing over our artifact ids
        project.afterEvaluate { project ->
          it.artifactId = artifactId.forMultiplatform(it, project)
        }
      } else {
        it.artifactId = artifactId
      }
    }
  }

  private fun String.forMultiplatform(publication: MavenPublication, project: Project): String {
    val projectName = project.name
    return if (publication.artifactId == projectName) {
      this
    } else if (publication.artifactId.startsWith("$projectName-")) {
      // Publications for specific platform targes use derived artifact ids (e.g. library, library-jvm,
      // library-js) and the suffix needs to be preserved
      publication.artifactId.replace("$projectName-", "$this-")
    } else {
      throw IllegalStateException(
        "The plugin can't handle the publication ${publication.name} artifactId " +
          "${publication.artifactId} in project $projectName"
      )
    }
  }

  private fun version(version: String) {
    this.version.set(version)
    this.version.finalizeValueOnRead()

    project.mavenPublications {
      it.version = this.version.get()
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
    project.mavenPublications { publication ->
      // TODO without afterEvaluate https://github.com/gradle/gradle/issues/12259 will happen
      project.afterEvaluate {
        publication.pom(configure)
      }
    }
  }

  /**
   * Configures the POM through Gradle properties.
   */
  @Incubating
  fun pomFromGradleProperties() {
    pomFromProperties.set(true)
    pomFromProperties.finalizeValue()

    val groupId = project.findOptionalProperty("GROUP")
    if (groupId != null) {
      groupId(groupId)
    }
    val artifactId = project.findOptionalProperty("POM_ARTIFACT_ID")
    if (artifactId != null) {
      artifactId(artifactId)
    }
    val version = project.findOptionalProperty("VERSION_NAME")
    if (version != null) {
      version(version)
    }

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
