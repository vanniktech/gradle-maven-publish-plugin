package com.vanniktech.maven.publish

import org.gradle.api.Action
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningPlugin

@Incubating
@Suppress("UnnecessaryAbstractClass")
abstract class MavenPublishBaseExtension(
  private val project: Project
) {

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
   * `gpg2 --export-secret-keys --armor KEY_ID` can be used to export they key for this. The in memory properties can
   * also be provided as environment variables by prefixing them with `ORG_GRADLE_PROJECT_`, e.g.
   * `ORG_GRADLE_PROJECT_signingInMemoryKey`.
   *
   * More information about signing as well as different ways to provide credentials
   * can be found in the [Gradle documentation](https://docs.gradle.org/current/userguide/signing_plugin.html)
   */
  // TODO update in memory set up once https://github.com/gradle/gradle/issues/16056 is implemented
  @Incubating
  fun signAllPublications() {
    project.plugins.apply(SigningPlugin::class.java)
    project.gradleSigning.setRequired(project.isSigningRequired)
    project.gradleSigning.sign(project.gradlePublishing.publications)

    val inMemoryKey = project.findOptionalProperty("signingInMemoryKey")
    if (inMemoryKey != null) {
      val inMemoryKeyId = project.findOptionalProperty("signingInMemoryKeyId")
      val inMemoryKeyPassword = project.findOptionalProperty("signingInMemoryKeyPassword")
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
}
