package com.vanniktech.maven.publish

/**
 * TODO .
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
 * TODO .
 */
sealed class JavadocJar {
  object None : JavadocJar()
  object Empty : JavadocJar()
  object Javadoc : JavadocJar()
  data class Dokka(val taskName: String) : JavadocJar()
}
