package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.tasks.JavadocJar.Companion.javadocJarTask
import com.vanniktech.maven.publish.tasks.SourcesJar.Companion.androidSourcesJar
import com.vanniktech.maven.publish.tasks.SourcesJar.Companion.emptySourcesJar
import com.vanniktech.maven.publish.tasks.SourcesJar.Companion.javaSourcesJar
import com.vanniktech.maven.publish.tasks.SourcesJar.Companion.kotlinSourcesJar
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider

/**
 * Represents a platform that the plugin supports to publish. For example [JavaLibrary], [AndroidLibrary] or
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
  override val sourcesJar: Boolean = true
) : Platform() {

  override fun configure(project: Project) {
    project.gradlePublishing.publications.create(PUBLICATION_NAME, MavenPublication::class.java) {
      it.from(project.components.getByName("java"))
      it.withSourcesJar { project.javaSourcesJar(sourcesJar) }
      it.withJavadocJar { project.javadocJarTask(javadocJar) }
    }
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
  override val sourcesJar: Boolean = true
) : Platform() {

  override fun configure(project: Project) {
    project.gradlePublishing.publications.withType(MavenPublication::class.java).all {
      if (it.name == "pluginMaven") {
        it.withSourcesJar { project.javaSourcesJar(sourcesJar) }
        it.withJavadocJar { project.javadocJarTask(javadocJar) }
      }
    }
  }
}

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
data class AndroidLibrary @JvmOverloads constructor(
  override val javadocJar: JavadocJar,
  override val sourcesJar: Boolean = true,
  val variant: String = "release"
) : Platform() {

  override fun configure(project: Project) {
    project.afterEvaluate {
      val component = project.components.findByName(variant) ?: throw MissingVariantException(variant)
      project.gradlePublishing.publications.create(PUBLICATION_NAME, MavenPublication::class.java) {
        it.from(component)
        it.withSourcesJar { project.androidSourcesJar(sourcesJar) }
        it.withJavadocJar { project.javadocJarTask(javadocJar, android = true) }
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
 * `n/a`
 * This does not include javadoc jars because there are no APIs for that available.
 */
data class KotlinMultiplatform @JvmOverloads constructor(
  override val javadocJar: JavadocJar = JavadocJar.Empty()
) : Platform() {
  // Automatically added by Kotlin MPP plugin.
  override val sourcesJar = false

  override fun configure(project: Project) {
    val javadocJarTask = project.javadocJarTask(javadocJar)

    project.gradlePublishing.publications.withType(MavenPublication::class.java).all {
      it.withJavadocJar { javadocJarTask }

      // On Kotlin versions before 1.4.30 sources jars are only created for platforms, not the common artifact.
      if (it.name == "kotlinMultiplatform") {
        val sourceArtifact = it.artifacts.find { artifact -> artifact.classifier == "sources" }
        if (sourceArtifact == null) {
          it.withSourcesJar { project.emptySourcesJar() }
        }
      }
    }
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
 *     from(components["java"])
 *     artifact(project.tasks.named("kotlinSourcesJar"))
 *   }
 * }
 * ```
 * This does not include javadoc jars because there are no APIs for that available.
 */
data class KotlinJvm @JvmOverloads constructor(
  override val javadocJar: JavadocJar = JavadocJar.Empty(),
  override val sourcesJar: Boolean = true
) : Platform() {

  override fun configure(project: Project) {
    // Create publication, since Kotlin/JS doesn't provide one by default.
    // https://youtrack.jetbrains.com/issue/KT-41582
    project.gradlePublishing.publications.create(PUBLICATION_NAME, MavenPublication::class.java) {
      it.from(project.components.getByName("java"))
      it.withSourcesJar { project.javaSourcesJar(sourcesJar) }
      it.withJavadocJar { project.javadocJarTask(javadocJar) }
    }
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

data class KotlinJs @JvmOverloads constructor(
  override val javadocJar: JavadocJar = JavadocJar.Empty(),
  override val sourcesJar: Boolean = true
) : Platform() {

  override fun configure(project: Project) {
    // Create publication, since Kotlin/JS doesn't provide one by default.
    // https://youtrack.jetbrains.com/issue/KT-41582
    project.gradlePublishing.publications.create("mavenJs", MavenPublication::class.java) {
      it.from(project.components.getByName("kotlin"))
      it.withSourcesJar { project.kotlinSourcesJar(sourcesJar) }
      it.withJavadocJar { project.javadocJarTask(javadocJar) }
    }
  }
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
  data class Dokka(val taskName: String) : JavadocJar()
}

private const val PUBLICATION_NAME = "maven"

private fun MavenPublication.withSourcesJar(factory: () -> TaskProvider<*>) {
  val task = factory()
  artifact(task)
}

private fun MavenPublication.withJavadocJar(factory: () -> TaskProvider<*>?) {
  val task = factory()
  if (task != null) {
    artifact(task)
  }
}

private class MissingVariantException(name: String) : RuntimeException(
  "Invalid MavenPublish Configuration. Unable to find variant to publish named $name." +
    " Try setting the 'androidVariantToPublish' property in the mavenPublish" +
    " extension object to something that matches the variant that ought to be published."
)
