package com.vanniktech.maven.publish

import com.android.build.api.dsl.LibraryExtension
import com.vanniktech.maven.publish.tasks.JavadocJar.Companion.javadocJarTask
import com.vanniktech.maven.publish.workaround.addTestFixturesSourcesJar
import com.vanniktech.maven.publish.workaround.fixTestFixturesMetadata
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget

/**
 * Represents a platform that the plugin supports to publish. For example [JavaLibrary], [AndroidMultiVariantLibrary] or
 * [KotlinMultiplatform]. When a platform is configured through [MavenPublishBaseExtension.configure] the plugin
 * will automatically set up the artifacts that should get published, including javadoc and sources jars depending
 * on the option.
 */
public sealed class Platform {
  public abstract val javadocJar: JavadocJar
  public abstract val sourcesJar: Boolean

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
public data class JavaLibrary @JvmOverloads constructor(
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
      it.withJavadocJar(javadocJar, project, multipleTasks = false)
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
public data class GradlePlugin @JvmOverloads constructor(
  override val javadocJar: JavadocJar,
  override val sourcesJar: Boolean = true,
) : Platform() {
  override fun configure(project: Project) {
    check(project.plugins.hasPlugin("java-gradle-plugin")) {
      "Calling configure(GradlePlugin(...)) requires the java-gradle-plugin to be applied"
    }

    project.mavenPublicationsWithoutPluginMarker {
      it.withJavaSourcesJar(sourcesJar, project)
      it.withJavadocJar(javadocJar, project, multipleTasks = false)
    }
  }
}

/**
 * To be used for `com.gradle.plugin-publish` projects. Uses the default publication that gets created by that plugin.
 */
public class GradlePublishPlugin : Platform() {
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
public data class AndroidSingleVariantLibrary @JvmOverloads constructor(
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
 * empty all variants will be published. Otherwise, only variants matching those filters will be included.
 *
 * Equivalent Gradle set up (AGP 7.1.1):
 * ```
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
 * ```
 */
public data class AndroidMultiVariantLibrary @JvmOverloads constructor(
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
 * To be used for `com.android.fused-library` projects. Applying this creates a publication for the library with
 * empty source and javadoc jars.
 *
 * Equivalent Gradle set up:
 * ```
 * publishing {
 *   publications {
 *     register<MavenPublication>("maven") {
 *       from(components["fusedLibraryComponent"])
 *     }
 *   }
 * }
 * ```
 */
@Incubating
public class AndroidFusedLibrary : Platform() {
  override val sourcesJar: Boolean = false
  override val javadocJar: JavadocJar = JavadocJar.Empty()

  override fun configure(project: Project) {
    check(project.plugins.hasPlugin("com.android.fused-library")) {
      "Calling configure(AndroidFusedLibrary(...)) requires the com.android.fused-library plugin to be applied"
    }

    project.mavenPublications {
      it.withJavadocJar(javadocJar, project, multipleTasks = false, configureArchives = true)
      it.withJavaSourcesJar(sourcesJar, project, configureArchives = true)
    }
  }

  override fun equals(other: Any?): Boolean = other is AndroidFusedLibrary

  override fun hashCode(): Int = this::class.hashCode()
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
public data class KotlinMultiplatform internal constructor(
  override val javadocJar: JavadocJar,
  override val sourcesJar: Boolean,
  val androidVariantsToPublish: List<String>,
  val forceAndroidVariantsIfNotEmpty: Boolean,
) : Platform() {
  @JvmOverloads public constructor(
    javadocJar: JavadocJar = JavadocJar.Empty(),
    sourcesJar: Boolean = true,
  ) : this(javadocJar, sourcesJar, emptyList(), forceAndroidVariantsIfNotEmpty = false)

  // TODO: deprecate when AGP 8.12 is stable
  public constructor(
    javadocJar: JavadocJar = JavadocJar.Empty(),
    sourcesJar: Boolean = true,
    androidVariantsToPublish: List<String>,
  ) : this(javadocJar, sourcesJar, androidVariantsToPublish, forceAndroidVariantsIfNotEmpty = true)

  override fun configure(project: Project) {
    check(project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
      "Calling configure(KotlinMultiplatform(...)) requires the org.jetbrains.kotlin.multiplatform plugin to be applied"
    }

    project.mavenPublications {
      it.withJavadocJar(javadocJar, project, multipleTasks = true)
    }

    project.extensions.configure(KotlinMultiplatformExtension::class.java) {
      it.withSourcesJar(sourcesJar)

      if (androidVariantsToPublish.isNotEmpty()) {
        it.targets.configureEach { target ->
          if (target is KotlinAndroidTarget) {
            if (forceAndroidVariantsIfNotEmpty || target.publishLibraryVariants.isNullOrEmpty()) {
              target.publishLibraryVariants = androidVariantsToPublish
            }
          }
        }
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
public data class KotlinJvm @JvmOverloads constructor(
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
      it.withJavaSourcesJar(sourcesJar, project)
      it.withJavadocJar(javadocJar, project, multipleTasks = false)
    }

    setupTestFixtures(project, sourcesJar)
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
public class JavaPlatform : Platform() {
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
public class VersionCatalog : Platform() {
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
public sealed interface JavadocJar {
  /**
   * Do not create a javadoc jar. This option is not compatible with Maven Central.
   */
  public class None : JavadocJar {
    override fun equals(other: Any?): Boolean = other is None

    override fun hashCode(): Int = this::class.hashCode()
  }

  /**
   * Creates an empty javadoc jar to satisfy maven central requirements.
   */
  public class Empty : JavadocJar {
    override fun equals(other: Any?): Boolean = other is Empty

    override fun hashCode(): Int = this::class.hashCode()
  }

  /**
   * Creates a regular javadoc jar using Gradle's default `javadoc` task.
   */
  public class Javadoc : JavadocJar {
    override fun equals(other: Any?): Boolean = other is Javadoc

    override fun hashCode(): Int = this::class.hashCode()
  }

  /**
   * Creates a javadoc jar using Dokka's output. The argument is the name of the dokka task that should be used
   * for that purpose.
   */
  public class Dokka private constructor(
    internal val wrapper: DokkaTaskWrapper,
  ) : JavadocJar {
    internal sealed interface DokkaTaskWrapper {
      fun asProvider(project: Project): Provider<*>
    }

    internal data class StringDokkaTaskWrapper(
      val value: String,
    ) : DokkaTaskWrapper {
      override fun asProvider(project: Project): Provider<*> = project.tasks.named(value)
    }

    internal data class StringProviderDokkaTaskWrapper(
      val value: Provider<String>,
    ) : DokkaTaskWrapper {
      override fun asProvider(project: Project): Provider<*> = value.flatMap { name -> project.tasks.named(name) }
    }

    internal data class DirectDokkaTaskWrapper(
      val value: TaskProvider<*>,
    ) : DokkaTaskWrapper {
      override fun asProvider(project: Project): Provider<*> = value
    }

    public constructor(taskName: String) : this(StringDokkaTaskWrapper(taskName))
    public constructor(taskName: Provider<String>) : this(StringProviderDokkaTaskWrapper(taskName))
    public constructor(task: TaskProvider<*>) : this(DirectDokkaTaskWrapper(task))

    override fun equals(other: Any?): Boolean = other is Dokka && wrapper == other.wrapper

    override fun hashCode(): Int = wrapper.hashCode()
  }
}

private const val PUBLICATION_NAME = "maven"

private fun MavenPublication.withJavaSourcesJar(enabled: Boolean, project: Project, configureArchives: Boolean = false) {
  if (enabled) {
    project.extensions.getByType(JavaPluginExtension::class.java).withSourcesJar()
  } else {
    val task = project.tasks.register("emptySourcesJar", Jar::class.java) {
      it.archiveClassifier.set("sources")
      if (configureArchives) {
        it.archiveBaseName.set(project.name)
        it.destinationDirectory.set(project.layout.buildDirectory.dir("libs"))
      }
    }
    artifact(task)
  }
}

private fun MavenPublication.withJavadocJar(
  javadocJar: JavadocJar,
  project: Project,
  multipleTasks: Boolean,
  configureArchives: Boolean = false,
) {
  val task = project.javadocJarTask(javadocJar, prefix = name.takeIf { multipleTasks })
  if (task != null) {
    artifact(task)

    if (configureArchives) {
      task.configure {
        it.destinationDirectory.set(project.layout.buildDirectory.dir("libs"))
      }
    }
  }
}

private fun setupTestFixtures(project: Project, sourcesJar: Boolean) {
  project.plugins.withId("java-test-fixtures") {
    if (sourcesJar) {
      addTestFixturesSourcesJar(project)
    }

    // test fixtures can't be mapped to the POM because there is no equivalent concept in Maven
    project.mavenPublications {
      it.suppressPomMetadataWarningsFor("testFixturesApiElements")
      it.suppressPomMetadataWarningsFor("testFixturesRuntimeElements")
      it.suppressPomMetadataWarningsFor("testFixturesSourcesElements")
    }

    fixTestFixturesMetadata(project)
  }
}

private class MissingVariantException(
  name: String,
) : RuntimeException(
    "Invalid MavenPublish Configuration. Unable to find variant to publish named $name." +
      " By default the publish plugin will publish the variant called \"release\". To modify this behavior" +
      " either call configure(AndroidSingleVariantLibrary(\"variant-to-publish\")) or " +
      " configure(AndroidMultiVariantLibrary()) to publish all flavors.",
  )
