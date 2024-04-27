package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.sonatype.CreateSonatypeRepositoryTask.Companion.registerCreateRepository
import com.vanniktech.maven.publish.sonatype.DropSonatypeRepositoryTask.Companion.registerDropRepository
import com.vanniktech.maven.publish.sonatype.ReleaseSonatypeRepositoryTask.Companion.registerReleaseRepository
import com.vanniktech.maven.publish.sonatype.SonatypeRepositoryBuildService.Companion.registerSonatypeRepositoryBuildService
import com.vanniktech.maven.publish.tasks.WorkaroundSignatureType
import org.gradle.api.Action
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningPlugin
import org.gradle.plugins.signing.type.pgp.ArmoredSignatureType
import org.gradle.util.GradleVersion
import org.jetbrains.dokka.gradle.DokkaTask

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
  internal val platform: Property<Platform> = project.objects.property(Platform::class.java)

  /**
   * Sets up Maven Central publishing through Sonatype OSSRH by configuring the target repository. Gradle will then
   * automatically create a `publishAllPublicationsToMavenCentralRepository` task as well as include it in the general
   * `publish` task. As part of running publish the plugin will automatically create a staging repository on Sonatype
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

    val buildService = project.registerSonatypeRepositoryBuildService(
      sonatypeHost = sonatypeHost,
      groupId = groupId,
      versionIsSnapshot = version.map { it.endsWith("-SNAPSHOT") },
      repositoryUsername = project.providers.gradleProperty("mavenCentralUsername"),
      repositoryPassword = project.providers.gradleProperty("mavenCentralPassword"),
      automaticRelease = automaticRelease,
      // TODO: stop accessing rootProject https://github.com/gradle/gradle/pull/26635
      rootBuildDirectory = project.rootProject.layout.buildDirectory,
    )

    val configCacheEnabled = project.configurationCache()
    project.gradlePublishing.repositories.maven { repo ->
      repo.name = "mavenCentral"
      repo.setUrl(buildService.map { it.publishingUrl(configCacheEnabled) })
      if (!host.isCentralPortal) {
        repo.credentials(PasswordCredentials::class.java)
      }
    }

    val createRepository = project.tasks.registerCreateRepository(buildService)

    project.tasks.withType(PublishToMavenRepository::class.java).configureEach { publishTask ->
      if (publishTask.name.endsWith("ToMavenCentralRepository")) {
        publishTask.dependsOn(createRepository)
      }
    }

    val releaseRepository = project.tasks.registerReleaseRepository(buildService, createRepository)
    project.tasks.registerDropRepository(buildService, createRepository)

    project.tasks.register("publishToMavenCentral") {
      it.description = "Publishes to a staging repository on Sonatype OSS"
      it.group = "release"
      it.dependsOn(project.tasks.named("publishAllPublicationsToMavenCentralRepository"))
    }
    project.tasks.register("publishAndReleaseToMavenCentral") {
      it.description = "Publishes to a staging repository on Sonatype OSS and releases it to MavenCentral"
      it.group = "release"
      it.dependsOn(project.tasks.named("publishAllPublicationsToMavenCentralRepository"))
      it.dependsOn(releaseRepository)
    }
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
   * Alternatively an in memory key can be used by exporting an ascii-armored GPG key and setting these Gradle properties:
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
  fun signAllPublications() {
    signing.set(true)
    signing.finalizeValue()

    project.plugins.apply(SigningPlugin::class.java)
    project.gradleSigning.setRequired(version.map { !it.endsWith("-SNAPSHOT") })

    // TODO update in memory set up once https://github.com/gradle/gradle/issues/16056 is implemented
    val inMemoryKey = project.findOptionalProperty("signingInMemoryKey")
    if (inMemoryKey != null) {
      val inMemoryKeyId = project.findOptionalProperty("signingInMemoryKeyId")
      val inMemoryKeyPassword = project.findOptionalProperty("signingInMemoryKeyPassword").orEmpty()
      project.gradleSigning.useInMemoryPgpKeys(inMemoryKeyId, inMemoryKey, inMemoryKeyPassword)
    }

    project.mavenPublications { publication ->
      project.gradleSigning.sign(publication)
    }

    // TODO: https://youtrack.jetbrains.com/issue/KT-46466 https://github.com/gradle/gradle/issues/26091
    project.tasks.withType(AbstractPublishToMaven::class.java).configureEach { publishTask ->
      publishTask.dependsOn(project.tasks.withType(Sign::class.java))
    }

    // TODO: https://youtrack.jetbrains.com/issue/KT-61313/ https://github.com/gradle/gradle/issues/26132
    project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
      project.tasks.withType(Sign::class.java).configureEach {
        it.signatureType = WorkaroundSignatureType(
          it.signatureType ?: ArmoredSignatureType(),
          project.layout.buildDirectory.dir("signatures/${it.name}"),
        )
      }
    }
  }

  /**
   * Set the Maven coordinates consisting of [groupId], [artifactId] and [version] for this project. In the case of
   * Kotlin Multiplatform projects the given [artifactId] is used together with the platform targets resulting in
   * artifactIds like `[artifactId]-jvm`.
   */
  fun coordinates(groupId: String? = null, artifactId: String? = null, version: String? = null) {
    groupId?.also { groupId(it) }
    artifactId?.also { artifactId(it) }
    version?.also { version(it) }
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
      // Publications for specific platform targets use derived artifact ids (e.g. library, library-jvm,
      // library-js) and the suffix needs to be preserved
      publication.artifactId.replace("$projectName-", "$this-")
    } else {
      throw IllegalStateException(
        "The plugin can't handle the publication ${publication.name} artifactId " +
          "${publication.artifactId} in project $projectName",
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
  fun pom(configure: Action<in MavenPom>) {
    project.mavenPublications { publication ->
      if (GradleVersion.current() >= GradleVersion.version("8.8-rc-1")) {
        publication.pom(configure)
      } else {
        project.afterEvaluate {
          publication.pom(configure)
        }
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
  fun configure(platform: Platform) {
    this.platform.set(platform)
    this.platform.finalizeValue()

    platform.configure(project)
  }

  /**
   * Calls [configure] with a [Platform] chosen based on other applied Gradle plugins.
   */
  @Incubating
  @JvmOverloads
  fun configureBasedOnAppliedPlugins(sourcesJar: Boolean = true, javadocJar: Boolean = true) {
    // has already been called before by the user or from finalizeDsl
    if (platform.isPresent) {
      return
    }

    when {
      project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform") -> {
        val variant = project.findOptionalProperty("ANDROID_VARIANT_TO_PUBLISH") ?: "release"
        configure(
          KotlinMultiplatform(
            javadocJar = defaultJavaDocOption(javadocJar, plainJavadocSupported = false),
            sourcesJar = sourcesJar,
            androidVariantsToPublish = listOf(variant),
            forceAndroidVariantsIfNotEmpty = false,
          ),
        )
      }
      project.plugins.hasPlugin("com.android.library") -> {
        val variant = project.findOptionalProperty("ANDROID_VARIANT_TO_PUBLISH") ?: "release"
        configure(AndroidSingleVariantLibrary(variant, sourcesJar, javadocJar))
      }
      project.plugins.hasPlugin("com.gradle.plugin-publish") ->
        configure(GradlePublishPlugin())
      project.plugins.hasPlugin("java-gradle-plugin") ->
        configure(GradlePlugin(defaultJavaDocOption(javadocJar, plainJavadocSupported = true), sourcesJar))
      project.plugins.hasPlugin("org.jetbrains.kotlin.jvm") ->
        configure(KotlinJvm(defaultJavaDocOption(javadocJar, plainJavadocSupported = true), sourcesJar))
      project.plugins.hasPlugin("java-library") ->
        configure(JavaLibrary(defaultJavaDocOption(javadocJar, plainJavadocSupported = true), sourcesJar))
      project.plugins.hasPlugin("java") ->
        configure(JavaLibrary(defaultJavaDocOption(javadocJar, plainJavadocSupported = true), sourcesJar))
      project.plugins.hasPlugin("java-platform") ->
        configure(JavaPlatform())
      project.plugins.hasPlugin("version-catalog") ->
        configure(VersionCatalog())
      else -> project.logger.warn("No compatible plugin found in project ${project.path} for publishing")
    }
  }

  private fun defaultJavaDocOption(javadocJar: Boolean, plainJavadocSupported: Boolean): JavadocJar {
    return if (!javadocJar) {
      JavadocJar.None()
    } else if (project.plugins.hasPlugin("org.jetbrains.dokka") || project.plugins.hasPlugin("org.jetbrains.dokka-android")) {
      val dokkaTask = project.provider {
        val tasks = project.tasks.withType(DokkaTask::class.java)
        tasks.singleOrNull()?.name ?: "dokkaHtml"
      }
      JavadocJar.Dokka(dokkaTask)
    } else if (plainJavadocSupported) {
      project.tasks.withType(Javadoc::class.java).configureEach {
        val options = it.options as StandardJavadocDocletOptions
        val javaVersion = project.javaVersion()
        if (javaVersion.isJava9Compatible) {
          options.addBooleanOption("html5", true)
        }
        if (javaVersion.isJava8Compatible) {
          options.addStringOption("Xdoclint:none", "-quiet")
        }
      }
      return JavadocJar.Javadoc()
    } else {
      JavadocJar.Empty()
    }
  }
}
