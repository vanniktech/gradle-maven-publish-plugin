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
import org.gradle.plugins.signing.SigningPlugin

@Incubating
@Suppress("UnnecessaryAbstractClass")
abstract class MavenPublishBaseExtension(
  private val project: Project
) {

  private var nexusOptions: NexusOptions? = null

  private var mavenCentral: Pair<SonatypeHost, String?>? = null
  private var signing: Boolean? = null
  private var platform: Platform? = null

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
   *
   * @param host the instance of Sonatype OSSRH to use
   * @param stagingRepositoryId optional parameter to upload to a specific already created staging repository
   */
  @Incubating
  @JvmOverloads
  fun publishToMavenCentral(host: SonatypeHost, stagingRepositoryId: String? = null) {
    val mavenCentral = mavenCentral
    if (mavenCentral != null) {
      // Ignore subsequent calls with the same arguments.
      if (mavenCentral.first == host || mavenCentral.second == stagingRepositoryId) {
        return
      }

      throw IllegalArgumentException("Called publishToMavenCentral more than once with different arguments")
    }

    this.mavenCentral = host to stagingRepositoryId

    project.gradlePublishing.repositories.maven { repo ->
      repo.name = "mavenCentral"
      if (stagingRepositoryId != null) {
        repo.setUrl("${host.rootUrl}/service/local/staging/deployByRepositoryId/$stagingRepositoryId/")
      } else {
        repo.setUrl("${host.rootUrl}/service/local/staging/deploy/maven2/")
      }
      repo.credentials(PasswordCredentials::class.java)

      project.afterEvaluate {
        if (it.version.toString().endsWith("SNAPSHOT")) {
          if (stagingRepositoryId != null) {
            throw IllegalArgumentException("Staging repositories are not supported for SNAPSHOT versions.")
          }
          repo.setUrl("${host.rootUrl}/content/repositories/snapshots/")
        }
      }
    }

    nexusOptions {
      it.baseUrl = "${host.rootUrl}/service/local/"
      it.repositoryUsername = project.findOptionalProperty("mavenCentralRepositoryUsername")
      it.repositoryPassword = project.findOptionalProperty("mavenCentralRepositoryPassword")
    }
  }

  internal fun nexusOptions(action: Action<NexusOptions>) {
    var nexusOptions = this.nexusOptions
    if (nexusOptions == null) {
      nexusOptions = checkNotNull(project.objects.newInstance(NexusOptions::class.java))
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
   * signingInMemoryPassword=secret
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
  @Incubating
  fun signAllPublications() {
    val signing = signing
    if (signing == true) {
      // ignore subsequent calls with the same arguments
      return
    }

    this.signing = true

    project.plugins.apply(SigningPlugin::class.java)
    project.gradleSigning.setRequired(project.isSigningRequired)
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
   * Configures a [Platform] which will automatically set up the artifacts that should get published, including javadoc
   * and sources jars depending on the option.
   */
  @Incubating
  fun configure(platform: Platform) {
    if (this.platform != null) {
      // Ignore subsequent calls with the same arguments.
      if (this.platform == platform) {
        return
      }

      throw IllegalArgumentException("Called configure(Platform) more than once with different arguments")
    }

    this.platform = platform

    val configurer = MavenPublishConfigurer(project)
    return when (platform) {
      is JavaLibrary -> configurer.configureJavaArtifacts(platform.sourcesJar, platform.javadocJar)
      is GradlePlugin -> configurer.configureGradlePluginProject(platform.sourcesJar, platform.javadocJar)
      is AndroidLibrary -> configurer.configureAndroidArtifacts(platform.variant, platform.sourcesJar, platform.javadocJar)
      is KotlinMultiplatform -> configurer.configureKotlinMppProject(platform.javadocJar)
      is KotlinJs -> configurer.configureKotlinJsProject(platform.sourcesJar, platform.javadocJar)
      is KotlinJvm -> configurer.configureJavaArtifacts(platform.sourcesJar, platform.javadocJar)
    }
  }
}
