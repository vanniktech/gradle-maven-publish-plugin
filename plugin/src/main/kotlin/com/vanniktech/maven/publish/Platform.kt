package com.vanniktech.maven.publish

import com.android.build.api.dsl.LibraryExtension
import com.vanniktech.maven.publish.tasks.JavadocJar.Companion.javadocJarTask
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.attributes.DocsType
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.internal.JavaConfigurationVariantMapping
import org.gradle.api.plugins.internal.JavaPluginHelper
import org.gradle.api.plugins.internal.JvmPluginsHelper
import org.gradle.api.plugins.jvm.internal.JvmPluginServices
import org.gradle.api.provider.Provider
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.serviceOf
import org.gradle.internal.component.external.model.ProjectDerivedCapability
import org.gradle.jvm.component.internal.DefaultJvmSoftwareComponent
import org.gradle.jvm.tasks.Jar
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Represents a platform that the plugin supports to publish. For example [JavaLibrary], [AndroidMultiVariantLibrary] or
 * [KotlinMultiplatform]. When a platform is configured through [MavenPublishBaseExtension.configure] the plugin
 * will automatically set up the artifacts that should get published, including javadoc and sources jars depending
 * on the option.
 */
sealed class Platform {
  abstract val javadocJar: JavadocJar
  abstract val sourcesJar: Boolean

  internal abstract fun configure(project: Project)
}

/**
 * To be used for `java` and `java-library` projects. Applying this creates a publication for the component called
 * `java`. Depending on the passed parameters for [javadocJar] and [sourcesJar], `-javadoc` and `-sources` jars will
 * be added to the publication.
 *
 * Equivalent Gradle set up:
 * ```
 * publishing {
 *   publications {
 *     create<MavenPublication>("maven") {
 *       from(components["java"])
 *     }
 *   }
 * }
 *
 * java {
 *   withSourcesJar()
 *   withJavadocJar()
 * }
 ```
 */
data class JavaLibrary @JvmOverloads constructor(
  override val javadocJar: JavadocJar,
  override val sourcesJar: Boolean = true,
) : Platform() {
  override fun configure(project: Project) {
    check(project.plugins.hasPlugin("java") || project.plugins.hasPlugin("java-library")) {
      "Calling configure(JavaLibrary(...)) requires the java-library plugin to be applied"
    }

    project.gradlePublishing.publications.create(PUBLICATION_NAME, MavenPublication::class.java) {
      it.from(project.components.getByName("java"))
      it.withJavaSourcesJar(sourcesJar, project)
      it.withJavadocJar { project.javadocJarTask(javadocJar) }
    }

    setupTestFixtures(project, sourcesJar)
  }
}

/**
 * To be used for `java-gradle-plugin` projects. Uses the default publication that gets created by that plugin.
 * Depending on the passed parameters for [javadocJar] and [sourcesJar], `-javadoc` and `-sources` jars will be added to
 * the publication.
 *
 * Equivalent Gradle set up:
 * ```
 * java {
 *   withSourcesJar()
 *   withJavadocJar()
 * }
```
 */
data class GradlePlugin @JvmOverloads constructor(
  override val javadocJar: JavadocJar,
  override val sourcesJar: Boolean = true,
) : Platform() {
  override fun configure(project: Project) {
    check(project.plugins.hasPlugin("java-gradle-plugin")) {
      "Calling configure(GradlePlugin(...)) requires the java-gradle-plugin to be applied"
    }

    val javadocJarTask = project.javadocJarTask(javadocJar)

    project.mavenPublicationsWithoutPluginMarker {
      it.withJavaSourcesJar(sourcesJar, project)
      it.withJavadocJar { javadocJarTask }
    }
  }
}

/**
 * To be used for `com.gradle.plugin-publish` projects. Uses the default publication that gets created by that plugin.
 */
class GradlePublishPlugin : Platform() {
  override val javadocJar: JavadocJar = JavadocJar.Javadoc()
  override val sourcesJar: Boolean = true

  override fun configure(project: Project) {
    check(project.plugins.hasPlugin("com.gradle.plugin-publish")) {
      "Calling configure(GradlePublishPlugin()) requires the com.gradle.plugin-publish plugin to be applied"
    }

    // setup is fully handled by com.gradle.plugin-publish already
  }

  override fun equals(other: Any?): Boolean = other is GradlePublishPlugin

  override fun hashCode(): Int = this::class.hashCode()
}

/**
 * To be used for `com.android.library` projects. Applying this creates a publication for the component of the given
 * `variant`. Depending on the passed parameters for [javadocJar] and [sourcesJar], `-javadoc` and `-sources` jars will
 * be added to the publication.
 *
 * Equivalent Gradle set up:
 * ```
 * android {
 *   publishing {
 *    singleVariant("variant") {
 *      withSourcesJar()
 *      withJavadocJar()
 *    }
 *   }
 * }
 *
 * afterEvaluate {
 *   publishing {
 *     publications {
 *       create<MavenPublication>("variant") {
 *         from(components["variant"])
 *       }
 *     }
 *   }
 * }
 *```
 */
data class AndroidSingleVariantLibrary @JvmOverloads constructor(
  val variant: String = "release",
  override val sourcesJar: Boolean = true,
  val publishJavadocJar: Boolean = true,
) : Platform() {
  override val javadocJar: JavadocJar get() = throw UnsupportedOperationException()

  override fun configure(project: Project) {
    check(project.plugins.hasPlugin("com.android.library")) {
      "Calling configure(AndroidSingleVariantLibrary(...)) requires the com.android.library plugin to be applied"
    }

    val library = project.extensions.findByType(LibraryExtension::class.java)!!
    library.publishing {
      singleVariant(variant) {
        if (sourcesJar) {
          withSourcesJar()
        }
        if (publishJavadocJar) {
          withJavadocJar()
        }
      }
    }

    project.afterEvaluate {
      val component = project.components.findByName(variant) ?: throw MissingVariantException(variant)
      project.gradlePublishing.publications.create(PUBLICATION_NAME, MavenPublication::class.java) {
        it.from(component)
      }
    }
  }
}

/**
 * To be used for `com.android.library` projects. Applying this creates a publication for the component of the given
 * variants. Depending on the passed parameters for [javadocJar] and [sourcesJar], `-javadoc` and `-sources` jars will
 * be added to the publication.
 *
 * If the [includedBuildTypeValues] and [includedFlavorDimensionsAndValues] parameters are not provided or
 * empty all variants will be published. Otherwise only variants matching those filters will be included.
 *
 * Equivalent Gradle set up (AGP 7.1.1):
 * android {
 *   publishing {
 *    multipleVariants {
 *      allVariants() // or calls to includeBuildTypeValues and includeFlavorDimensionAndValues
 *      withSourcesJar()
 *      withJavadocJar()
 *    }
 *   }
 * }
 *
 * afterEvaluate {
 *   publishing {
 *     publications {
 *       create<MavenPublication>("default") {
 *         from(components["default"])
 *       }
 *     }
 *   }
 * }
 */
data class AndroidMultiVariantLibrary @JvmOverloads constructor(
  override val sourcesJar: Boolean = true,
  val publishJavadocJar: Boolean = true,
  val includedBuildTypeValues: Set<String> = emptySet(),
  val includedFlavorDimensionsAndValues: Map<String, Set<String>> = emptyMap(),
) : Platform() {
  override val javadocJar: JavadocJar get() = throw UnsupportedOperationException()

  override fun configure(project: Project) {
    check(project.plugins.hasPlugin("com.android.library")) {
      "Calling configure(AndroidMultiVariantLibrary(...)) requires the com.android.library plugin to be applied"
    }

    val library = project.extensions.findByType(LibraryExtension::class.java)!!
    library.publishing {
      multipleVariants(PUBLICATION_NAME) {
        if (includedBuildTypeValues.isEmpty() && includedFlavorDimensionsAndValues.isEmpty()) {
          allVariants()
        } else {
          if (includedBuildTypeValues.isNotEmpty()) {
            includeBuildTypeValues(*includedBuildTypeValues.toTypedArray())
          }
          includedFlavorDimensionsAndValues.forEach { (dimension, flavors) ->
            includeFlavorDimensionAndValues(dimension, *flavors.toTypedArray())
          }
        }

        if (sourcesJar) {
          withSourcesJar()
        }
        if (publishJavadocJar) {
          withJavadocJar()
        }
      }
    }

    project.afterEvaluate {
      val component = project.components.findByName(PUBLICATION_NAME) ?: throw MissingVariantException(PUBLICATION_NAME)
      project.gradlePublishing.publications.create(PUBLICATION_NAME, MavenPublication::class.java) {
        it.from(component)
      }
    }
  }
}

/**
 * To be used for `org.jetbrains.kotlin.multiplatform` projects. Uses the default publications that gets created by
 * that plugin, including the automatically created `-sources` jars. Depending on the passed parameters for [javadocJar],
 * `-javadoc` will be added to the publications.
 *
 * Equivalent Gradle set up:
 * ```
 * // Nothing to configure setup is automatic.
 * ```
 *
 * This does not include javadoc jars because there are no APIs for that available.
 */
data class KotlinMultiplatform @JvmOverloads constructor(
  override val javadocJar: JavadocJar = JavadocJar.Empty(),
  override val sourcesJar: Boolean = true,
) : Platform() {
  override fun configure(project: Project) {
    check(project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
      "Calling configure(KotlinMultiplatform(...)) requires the org.jetbrains.kotlin.multiplatform plugin to be applied"
    }

    val javadocJarTask = project.javadocJarTask(javadocJar)

    project.mavenPublications {
      it.withJavadocJar { javadocJarTask }
    }

    if (project.isAtLeastKotlinVersion("org.jetbrains.kotlin.multiplatform", 1, 9, 0)) {
      project.extensions.configure(KotlinMultiplatformExtension::class.java) {
        it.withSourcesJar(sourcesJar)
      }
    } else {
      check(sourcesJar) {
        "Disabling sources publishing for Kotlin/Multiplatform is not supported until Kotlin 1.9.0"
      }
    }
  }
}

/**
 * To be used for `org.jetbrains.kotlin.jvm` projects. Applying this creates a publication for the component called
 * `java`. Depending on the passed parameters for [javadocJar] and [sourcesJar], `-javadoc` and `-sources` jars will be
 * added to the publication.
 *
 * Equivalent Gradle set up:
 * ```
 * publications {
 *   create<MavenPublication>("maven") {
 *     from(components["java"])
 *     artifact(project.tasks.named("javaSourcesJar"))
 *   }
 * }
 * ```
 * This does not include javadoc jars because there are no APIs for that available.
 */
data class KotlinJvm @JvmOverloads constructor(
  override val javadocJar: JavadocJar = JavadocJar.Empty(),
  override val sourcesJar: Boolean = true,
) : Platform() {
  override fun configure(project: Project) {
    check(project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
      "Calling configure(KotlinJvm(...)) requires the org.jetbrains.kotlin.jvm plugin to be applied"
    }

    // Create publication, since Kotlin/JS doesn't provide one by default.
    // https://youtrack.jetbrains.com/issue/KT-41582
    project.gradlePublishing.publications.create(PUBLICATION_NAME, MavenPublication::class.java) {
      it.from(project.components.getByName("java"))
      it.withKotlinSourcesJar(sourcesJar, project)
      it.withJavadocJar { project.javadocJarTask(javadocJar) }
    }

    setupTestFixtures(project, sourcesJar)
  }
}

/**
 * To be used for `org.jetbrains.kotlin.js` projects. Applying this creates a publication for the component called
 * `kotlin`. Depending on the passed parameters for [javadocJar] and [sourcesJar], `-javadoc` and `-sources` jars will be
 * added to the publication.
 *
 * Equivalent Gradle set up:
 * ```
 * publications {
 *   create<MavenPublication>("maven") {
 *     from(components["kotlin"])
 *     artifact(project.tasks.named("kotlinSourcesJar"))
 *   }
 * }
 * ```
 * This does not include javadoc jars because there are no APIs for that available.
 */
@Deprecated("The Kotlin/JS plugin has been deprecated in Kotlin 1.9.0")
data class KotlinJs
  @Deprecated(
    "Disabling sources publishing for Kotlin/JS is not supported since Kotlin 1.8.20. " +
      "Use the single or no-arg constructors instead.",
  )
  constructor(
    override val javadocJar: JavadocJar,
    override val sourcesJar: Boolean,
  ) : Platform() {
    @Suppress("DEPRECATION")
    @JvmOverloads
    constructor(
      javadocJar: JavadocJar = JavadocJar.Empty(),
    ) : this(javadocJar, true)

    override fun configure(project: Project) {
      check(project.plugins.hasPlugin("org.jetbrains.kotlin.js")) {
        "Calling configure(KotlinJs(...)) requires the org.jetbrains.kotlin.js plugin to be applied"
      }

      // Create publication, since Kotlin/JS doesn't provide one by default.
      // https://youtrack.jetbrains.com/issue/KT-41582
      project.afterEvaluate {
        project.gradlePublishing.publications.create(PUBLICATION_NAME, MavenPublication::class.java) {
          it.from(project.components.getByName("kotlin"))
          if (project.isAtLeastKotlinVersion("org.jetbrains.kotlin.js", 1, 8, 20)) {
            check(sourcesJar) {
              "Disabling sources publishing for Kotlin/JS is not supported since Kotlin 1.8.20"
            }
          } else {
            it.withKotlinSourcesJar(sourcesJar, project)
          }
          it.withJavadocJar { project.javadocJarTask(javadocJar) }
        }
      }
    }
  }

/**
 * To be used for `java-platforms` projects. Applying this creates a publication for the component called
 * `javaPlatform`.
 *
 * Equivalent Gradle set up:
 * ```
 * publishing {
 *     publications {
 *         maven(MavenPublication) {
 *             from components.javaPlatform
 *         }
 *     }
 * }
 * ```
 */
class JavaPlatform : Platform() {
  override val javadocJar: JavadocJar = JavadocJar.None()
  override val sourcesJar: Boolean = false

  override fun configure(project: Project) {
    check(project.plugins.hasPlugin("java-platform")) {
      "Calling configure(JavaPlatform(...)) requires the java-platform plugin to be applied"
    }

    project.gradlePublishing.publications.create(PUBLICATION_NAME, MavenPublication::class.java) {
      it.from(project.components.getByName("javaPlatform"))
    }
  }

  override fun equals(other: Any?): Boolean = other is JavaPlatform

  override fun hashCode(): Int = this::class.hashCode()
}

/**
 * To be used for `version-catalog` projects. Applying this creates a publication for the component called
 * `versionCatalog`.
 *
 * Equivalent Gradle set up:
 * ```
 * publishing {
 *     publications {
 *         maven(MavenPublication) {
 *             from components.versionCatalog
 *         }
 *     }
 * }
 * ```
 */
class VersionCatalog : Platform() {
  override val javadocJar: JavadocJar = JavadocJar.None()
  override val sourcesJar: Boolean = false

  override fun configure(project: Project) {
    check(project.plugins.hasPlugin("version-catalog")) {
      "Calling configure(VersionCatalog(...)) requires the version-catalog plugin to be applied"
    }

    project.gradlePublishing.publications.create(PUBLICATION_NAME, MavenPublication::class.java) {
      it.from(project.components.getByName("versionCatalog"))
    }
  }

  override fun equals(other: Any?): Boolean = other is VersionCatalog

  override fun hashCode(): Int = this::class.hashCode()
}

/**
 * Specifies how the javadoc jar should be created.
 */
sealed class JavadocJar {
  /**
   * Do not create a javadoc jar. This option is not compatible with Maven Central.
   */
  class None : JavadocJar() {
    override fun equals(other: Any?): Boolean = other is None

    override fun hashCode(): Int = this::class.hashCode()
  }

  /**
   * Creates an empty javadoc jar to satisfy maven central requirements.
   */
  class Empty : JavadocJar() {
    override fun equals(other: Any?): Boolean = other is Empty

    override fun hashCode(): Int = this::class.hashCode()
  }

  /**
   * Creates a regular javadoc jar using Gradle's default `javadoc` task.
   */
  class Javadoc : JavadocJar() {
    override fun equals(other: Any?): Boolean = other is Javadoc

    override fun hashCode(): Int = this::class.hashCode()
  }

  /**
   * Creates a javadoc jar using Dokka's output. The argument is the name of the dokka task that should be used
   * for that purpose.
   */
  class Dokka private constructor(
    internal val taskName: DokkaTaskName,
  ) : JavadocJar() {
    internal sealed interface DokkaTaskName

    internal data class StringDokkaTaskName(val value: String) : DokkaTaskName

    internal data class ProviderDokkaTaskName(val value: Provider<String>) : DokkaTaskName

    constructor(taskName: String) : this(StringDokkaTaskName(taskName))
    constructor(taskName: Provider<String>) : this(ProviderDokkaTaskName(taskName))

    override fun equals(other: Any?): Boolean = other is Dokka && taskName == other.taskName

    override fun hashCode(): Int = taskName.hashCode()
  }
}

private const val PUBLICATION_NAME = "maven"

private fun MavenPublication.withKotlinSourcesJar(enabled: Boolean, project: Project) {
  val task = if (enabled) {
    project.tasks.named("kotlinSourcesJar")
  } else {
    project.emptySourcesJar()
  }
  artifact(task)
}

private fun MavenPublication.withJavaSourcesJar(enabled: Boolean, project: Project) {
  if (enabled) {
    project.extensions.getByType(JavaPluginExtension::class.java).withSourcesJar()
  } else {
    val task = project.emptySourcesJar()
    artifact(task)
  }
}

private fun Project.emptySourcesJar(): TaskProvider<*> = tasks.register("emptySourcesJar", Jar::class.java) {
  it.archiveClassifier.set("sources")
}

private fun MavenPublication.withJavadocJar(factory: () -> TaskProvider<*>?) {
  val task = factory()
  if (task != null) {
    artifact(task)
  }
}

private fun setupTestFixtures(project: Project, sourcesJar: Boolean) {
  project.plugins.withId("java-test-fixtures") {
    if (sourcesJar) {
      // TODO: remove after https://github.com/gradle/gradle/issues/20539 is resolved
      val testFixtureSourceSetName = "testFixtures"
      if (GradleVersion.current() >= GradleVersion.version("8.1")) {
        val extension = project.extensions.getByType(JavaPluginExtension::class.java)
        val testFixturesSourceSet = extension.sourceSets.maybeCreate(testFixtureSourceSetName)

        val sourceElements = JvmPluginsHelper.createDocumentationVariantWithArtifact(
          testFixturesSourceSet.sourcesElementsConfigurationName,
          testFixtureSourceSetName,
          DocsType.SOURCES,
          listOf(ProjectDerivedCapability(project, "testFixtures")),
          testFixturesSourceSet.sourcesJarTaskName,
          testFixturesSourceSet.allSource,
          project as ProjectInternal,
        )

        val component = JavaPluginHelper.getJavaComponent(project) as DefaultJvmSoftwareComponent
        component.addVariantsFromConfiguration(sourceElements, JavaConfigurationVariantMapping("compile", true))
      } else {
        val services = project.serviceOf<JvmPluginServices>()
        val action = Action<Any> {
          it.javaClass.getMethod("withSourcesJar").invoke(it)
          it.javaClass.getMethod("published").invoke(it)
        }
        val method = services.javaClass.getMethod("createJvmVariant", String::class.java, Action::class.java)
        method.invoke(services, testFixtureSourceSetName, action)
      }
    }

    // test fixtures can't be mapped to the POM because there is no equivalent concept in Maven
    project.mavenPublications {
      it.suppressPomMetadataWarningsFor("testFixturesApiElements")
      it.suppressPomMetadataWarningsFor("testFixturesRuntimeElements")
      it.suppressPomMetadataWarningsFor("testFixturesSourcesElements")
    }

    project.afterEvaluate {
      // Gradle will put the project group and version into capabilities instead of using
      // the publication, this can lead to invalid published metadata
      // TODO remove after https://github.com/gradle/gradle/issues/23354 is resolved
      project.group = project.baseExtension.groupId.get()
      project.version = project.baseExtension.version.get()
    }
  }
}

private class MissingVariantException(name: String) : RuntimeException(
  "Invalid MavenPublish Configuration. Unable to find variant to publish named $name." +
    " Try setting the 'androidVariantToPublish' property in the mavenPublish" +
    " extension object to something that matches the variant that ought to be published.",
)
