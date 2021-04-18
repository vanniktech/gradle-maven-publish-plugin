package com.vanniktech.maven.publish

/**
 * Represents a platform that the plugin supports to publish. For example [JavaLibrary], [AndroidLibrary] or
 * [KotlinMultiplatform]. When a platform is configured through [MavenPublishBaseExtension.configure] the plugin
 * will automatically set up the artifacts that should get published, including javadoc and sources jars depending
 * on the option.
 */
sealed class Platform {
  abstract val javadocJar: JavadocJar
  abstract val sourcesJar: Boolean
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
data class JavaLibrary(
  override val javadocJar: JavadocJar,
  override val sourcesJar: Boolean = true
) : Platform()

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
data class GradlePlugin(
  override val javadocJar: JavadocJar,
  override val sourcesJar: Boolean = true
) : Platform()

/**
 * To be used for `com.android.library` projects. Applying this creates a publication for the component of the given
 * `variant`. Depending on the passed parameters for [javadocJar] and [sourcesJar], `-javadoc` and `-sources` jars will
 * be added to the publication.
 *
 * Equivalent Gradle set up:
 * ```
 * afterEvaluate {
 *   publishing {
 *     publications {
 *       create<MavenPublication>("maven") {
 *         from(components["java"])
 *       }
 *     }
 *   }
 * }
 * ```
 * This does not include javadoc and sources jars because there are no APIs for that available.
 */
data class AndroidLibrary(
  override val javadocJar: JavadocJar,
  override val sourcesJar: Boolean = true,
  val variant: String = "release"
) : Platform()

/**
 * To be used for `org.jetbrains.kotlin.multiplatform` projects.  Uses the default publications that gets created by
 * that plugin, including the automatically created `-sources` jars. Depending on the passed parameters for [javadocJar],
 * `-javadoc` will be added to the publications.
 *
 * Equivalent Gradle set up:
 * `n/a`
 * This does not include javadoc jars because there are no APIs for that available.
 */
data class KotlinMultiplatform(
  override val javadocJar: JavadocJar = JavadocJar.Empty
) : Platform() {
  // automatically added by Kotlin MPP plugin
  override val sourcesJar = false
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
 *     from(components["java"])
 *     artifact(project.tasks.named("kotlinSourcesJar"))
 *   }
 * }
 * ```
 * This does not include javadoc jars because there are no APIs for that available.
  */
data class KotlinJvm(
  override val javadocJar: JavadocJar = JavadocJar.Empty,
  override val sourcesJar: Boolean = true
) : Platform()

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
data class KotlinJs(
  override val javadocJar: JavadocJar = JavadocJar.Empty,
  override val sourcesJar: Boolean = true
) : Platform()

/**
 * Specifies how the javadoc jar should be created.
 */
sealed class JavadocJar {
  /**
   * Do not create a javadoc jar. This option is not compatible with Maven Central.
   */
  object None : JavadocJar()

  /**
   * Creates an empty javadoc jar to satisfy maven central requirements.
   */
  object Empty : JavadocJar()

  /**
   * Creates a regular javadoc jar using Gradle's default `javadoc` task.
   */
  object Javadoc : JavadocJar()

  /**
   * Creates a javadoc jar using Dokka's output. The argument is the name of the dokka task that should be used
   * for that purpose.
   */
  data class Dokka(val taskName: String) : JavadocJar()
}
