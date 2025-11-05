package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.central.DropMavenCentralDeploymentTask.Companion.registerDropMavenCentralDeploymentTask
import com.vanniktech.maven.publish.central.EnableAutomaticMavenCentralPublishingTask.Companion.registerEnableAutomaticMavenCentralPublishingTask
import com.vanniktech.maven.publish.central.MavenCentralBuildService.Companion.registerMavenCentralBuildService
import com.vanniktech.maven.publish.central.PrepareMavenCentralPublishingTask.Companion.registerPrepareMavenCentralPublishingTask
import com.vanniktech.maven.publish.workaround.DirectorySignatureType
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.build.event.BuildEventsListenerRegistry
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningPlugin
import org.gradle.plugins.signing.type.pgp.ArmoredSignatureType
import org.jetbrains.dokka.gradle.DokkaTask

public abstract class MavenPublishBaseExtension @Inject constructor(
  private val project: Project,
  private val buildEventsListenerRegistry: BuildEventsListenerRegistry,
) {
  private val mavenCentral: Property<Boolean> = project.objects.property(Boolean::class.java)
  private val signing: Property<Boolean> = project.objects.property(Boolean::class.java)
  internal val groupId: Property<String> = project.objects
    .property(String::class.java)
    .convention(project.provider { project.group.toString() })
  internal val artifactId: Property<String> = project.objects
    .property(String::class.java)
    .convention(project.provider { project.name.toString() })
  internal val version: Property<String> = project.objects
    .property(String::class.java)
    .convention(project.provider { project.version.toString() })
  private val pomFromProperties: Property<Boolean> = project.objects.property(Boolean::class.java)
  private val platform: Property<Platform> = project.objects.property(Platform::class.java)

  /**
   * Sets up Maven Central publishing through Sonatype OSSRH by configuring the target repository. Gradle will then
   * automatically create a `publishAllPublicationsToMavenCentralRepository` task as well as include it in the general
   * `publish` task.
   *
   * When the [automaticRelease] parameter is `true` the created deployment will be released automatically to
   * Maven Central without any additional manual steps needed. When [automaticRelease] is not set or `false`
   * the deployment has to be manually released through the [Central Portal website](https://central.sonatype.com/publishing/deployments).
   *
   * If the current version ends with `-SNAPSHOT` the artifacts will be published to Sonatype's snapshot
   * repository instead.
   *
   * This expects you provide the username and password of a user token through Gradle properties called
   * `mavenCentralUsername` and `mavenCentralPassword`. See [here](https://central.sonatype.org/publish/generate-portal-token/)
   * for how to obtain a user token.
   *
   * When [validateDeployment] is `true` (the default), the plugin will monitor the deployment status after upload
   * and wait until it reaches a terminal state (`PUBLISHED` or `FAILED`). Deployment validation only happens
   * when [automaticRelease] is `true`.
   *
   * @param automaticRelease whether a non SNAPSHOT build should be released automatically at the end of the build
   * @param validateDeployment whether to wait for the deployment to be validated and published at the end of the build
   */
  @JvmOverloads
  public fun publishToMavenCentral(automaticRelease: Boolean = false, validateDeployment: Boolean = true) {
    mavenCentral.set(true)
    mavenCentral.finalizeValue()

    val localRepository = project.layout.buildDirectory.dir("publishing/mavenCentral")
    val versionIsSnapshot = version.map { it.endsWith("-SNAPSHOT") }

    val repository = project.gradlePublishing.repositories.maven { repo ->
      repo.name = "mavenCentral"
    }

    project.afterEvaluate {
      if (versionIsSnapshot.get()) {
        repository.setUrl("https://central.sonatype.com/repository/maven-snapshots/")
        repository.credentials(PasswordCredentials::class.java)
      } else {
        repository.setUrl(localRepository.get().asFile)
      }
    }

    val buildService = project.registerMavenCentralBuildService(
      repositoryUsername = project.providers.gradleProperty("mavenCentralUsername"),
      repositoryPassword = project.providers.gradleProperty("mavenCentralPassword"),
      rootBuildDirectory = @Suppress("UnstableApiUsage") project.layout.settingsDirectory.dir("build"),
      buildEventsListenerRegistry = buildEventsListenerRegistry,
    )

    val prepareTask = project.tasks.registerPrepareMavenCentralPublishingTask(buildService, groupId, artifactId, version, localRepository)
    val enableAutomaticTask = project.tasks.registerEnableAutomaticMavenCentralPublishingTask(buildService, validateDeployment)

    project.tasks.withType(PublishToMavenRepository::class.java).configureEach { publishTask ->
      if (publishTask.name.endsWith("ToMavenCentralRepository")) {
        publishTask.dependsOn(prepareTask)
        if (automaticRelease) {
          publishTask.dependsOn(enableAutomaticTask)
        }
      }
    }

    project.tasks.register("publishToMavenCentral") {
      it.description = "Publishes to Maven Central"
      it.group = "publishing"
      it.dependsOn(project.tasks.named("publishAllPublicationsToMavenCentralRepository"))
    }
    project.tasks.register("publishAndReleaseToMavenCentral") {
      it.description = "Publishes to Maven Central and automatically triggers release"
      it.group = "publishing"
      it.dependsOn(project.tasks.named("publishAllPublicationsToMavenCentralRepository"))
      it.dependsOn(enableAutomaticTask)
    }

    project.tasks.registerDropMavenCentralDeploymentTask(buildService)
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
  public fun signAllPublications() {
    signing.set(true)
    signing.finalizeValue()

    project.plugins.apply(SigningPlugin::class.java)
    project.gradleSigning.setRequired(version.map { !it.endsWith("-SNAPSHOT") })

    // TODO update in memory set up once https://github.com/gradle/gradle/issues/16056 is implemented
    val inMemoryKey = project.providers.gradleProperty("signingInMemoryKey")
    if (inMemoryKey.isPresent) {
      val inMemoryKeyId = project.providers.gradleProperty("signingInMemoryKeyId")
      val inMemoryKeyPassword = project.providers.gradleProperty("signingInMemoryKeyPassword").orElse("")
      project.gradleSigning.useInMemoryPgpKeys(inMemoryKeyId.orNull, inMemoryKey.get(), inMemoryKeyPassword.get())
    }

    project.mavenPublications { publication ->
      project.gradleSigning.sign(publication)
    }

    // TODO: https://youtrack.jetbrains.com/issue/KT-61313/ https://github.com/gradle/gradle/issues/26132
    project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
      project.tasks.withType(Sign::class.java).configureEach {
        it.signatureType = DirectorySignatureType(
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
  public fun coordinates(groupId: String? = null, artifactId: String? = null, version: String? = null) {
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
    this.artifactId.set(artifactId)
    this.artifactId.finalizeValueOnRead()

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
  public fun pom(configure: Action<in MavenPom>) {
    project.mavenPublications { publication ->
      publication.pom(configure)
    }
  }

  /**
   * Configures the POM through Gradle properties.
   */
  // TODO: we can't call 'providers.gradleProperty' instead due to
  //  https://github.com/gradle/gradle/issues/23572
  //  https://github.com/gradle/gradle/issues/29600
  @Suppress(
    "GradleProjectIsolation",
  )
  @Incubating
  public fun pomFromGradleProperties() {
    fun Project.findOptionalProperty(propertyName: String) = findProperty(propertyName)?.toString()

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
  public fun configure(platform: Platform) {
    this.platform.set(platform)
    this.platform.finalizeValue()

    platform.configure(project)
  }

  /**
   * Calls [configure] with a [Platform] chosen based on other applied Gradle plugins.
   */
  @Incubating
  @JvmOverloads
  public fun configureBasedOnAppliedPlugins(sourcesJar: Boolean = true, javadocJar: Boolean = true) {
    // has already been called before by the user or from finalizeDsl
    if (platform.isPresent) {
      return
    }

    when {
      project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform") -> {
        val variants = if (project.plugins.hasPlugin("com.android.kotlin.multiplatform.library")) {
          emptyList()
        } else {
          listOf(project.providers.gradleProperty("ANDROID_VARIANT_TO_PUBLISH").orNull ?: "release")
        }
        configure(
          KotlinMultiplatform(
            javadocJar = defaultJavaDocOption(javadocJar, plainJavadocSupported = false),
            sourcesJar = sourcesJar,
            androidVariantsToPublish = variants,
            forceAndroidVariantsIfNotEmpty = false,
          ),
        )
      }
      project.plugins.hasPlugin("com.android.library") -> {
        val variant = project.providers.gradleProperty("ANDROID_VARIANT_TO_PUBLISH").orNull ?: "release"
        configure(AndroidSingleVariantLibrary(variant, sourcesJar, javadocJar))
      }
      project.plugins.hasPlugin("com.android.fused-library") -> {
        configure(AndroidFusedLibrary())
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
    } else if (project.plugins.hasPlugin("org.jetbrains.dokka-javadoc")) {
      JavadocJar.Dokka("dokkaGeneratePublicationJavadoc")
    } else if (project.plugins.hasPlugin("org.jetbrains.dokka")) {
      // only dokka v2 has an extension
      if (project.extensions.findByName("dokka") != null) {
        JavadocJar.Dokka("dokkaGeneratePublicationHtml")
      } else {
        val dokkaTask = project.provider {
          val tasks = project.tasks.withType(DokkaTask::class.java)
          tasks.singleOrNull()?.name ?: "dokkaHtml"
        }
        JavadocJar.Dokka(dokkaTask)
      }
    } else if (plainJavadocSupported) {
      project.tasks.withType(Javadoc::class.java).configureEach {
        val options = it.options as StandardJavadocDocletOptions
        val javaVersion = project.javaVersion()
        if (javaVersion.isJava9Compatible) {
          options.addBooleanOption("html5", true)
        }
      }
      return JavadocJar.Javadoc()
    } else {
      JavadocJar.Empty()
    }
  }
}
