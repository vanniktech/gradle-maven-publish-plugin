# Configuring what to publish

It is possible to configure publishing for the following Gradle plugins:
- `com.android.library` as [single variant library](#android-library-single-variant) or
  as [multi variant library](#android-library-multiple-variants)
- [`org.jetbrains.kotlin.jvm`](#kotlin-jvm-library)
- [`org.jetbrains.kotlin.multiplatform`](#kotlin-multiplatform-library)
- [`java`](#java-library)
- [`java-library`](#java-library)
- [`java-gradle-plugin`](#gradle-plugin)
- [`com.gradle.plugin-publish`](#gradle-publish-plugin)
- [`java-platform`](#java-platform)
- [`version-catalog`](#version-catalog)

## Android Library (multiple variants)

For projects using the `com.android.library` plugin. This will publish all variants of the project (e.g. both
`debug` and `release`) or a subset of specified variants.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.AndroidMultiVariantLibrary

    mavenPublishing {
      // the first parameter represents whether to publish a sources jar
      // the second whether to publish a javadoc jar
      configure(new AndroidMultiVariantLibrary(true, true))
      // or to limit which build types to include
      configure(new AndroidMultiVariantLibrary(true, true, ["beta", "release"] as Set))
      // or to limit which flavors to include, the map key is a flavor dimension, the set contains the flavors
      configure(new AndroidMultiVariantLibrary(true, true, ["beta", "release"] as Set, ["store": ["google", "samsung"] as Set]))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.AndroidMultiVariantLibrary

    mavenPublishing {
      configure(AndroidMultiVariantLibrary(
        // whether to publish a sources jar
        sourcesJar = true,
        // whether to publish a javadoc jar
        publishJavadocJar = true,
      ))
      // or
      configure(AndroidMultiVariantLibrary(
        // whether to publish a sources jar
        sourcesJar = true,
        // whether to publish a javadoc jar
        publishJavadocJar = true,
        // limit which build types to include
        includedBuildTypeValues = setOf("beta", "release"),
      ))
      // or
      configure(AndroidMultiVariantLibrary(
        // whether to publish a sources jar
        sourcesJar = true,
        // whether to publish a javadoc jar
        publishJavadocJar = true,
        // limit which build types to include
        includedBuildTypeValues = setOf("beta", "release"),
        // limit which flavors to include, the map key is a flavor dimension, the set contains the flavors
        includedFlavorDimensionsAndValues = mapOf("store" to setOf("google", "samsung")),
      ))
    }
    ```

## Android Library (single variant)

For projects using the `com.android.library` plugin. Compared to the multi variant version above this will only publish
the specified variant instead of publishing all of them.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

    mavenPublishing {
      // the first parameter represennts which variant is published
      // the second whether to publish a sources jar
      // the third whether to publish a javadoc jar
      configure(new AndroidSingleVariantLibrary("release", true, true))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

    mavenPublishing {
      configure(AndroidSingleVariantLibrary(
        // the published variant
        variant = "release",
        // whether to publish a sources jar
        sourcesJar = true,
        // whether to publish a javadoc jar
        publishJavadocJar = true,
      ))
    }
    ```


## Android Fused Library

For projects using the `com.android.fused-library` plugin.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

    mavenPublishing {
      configure(new AndroidFusedLibrary())
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.AndroidFusedLibrary

    mavenPublishing {
      configure(AndroidFusedLibrary())
    }
    ```

!!! warning

    Support for Android fused libraries is considered experimental and might break
    with future Android Gradle plugin updates.

!!! warning

    Signing is currently unsupported for Android fused libraries.

!!! info

    Configuring source and javadoc publishing is currently not possible and the
    plugin will always publush empty jars for them.


## Java Library

For projects using the `java-library` plugin.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.JavaLibrary
    import com.vanniktech.maven.publish.JavadocJar

    mavenPublishing {
      // the first parameter configures the -javadoc artifact, possible values:
      // - `JavadocJar.None()` don't publish this artifact
      // - `JavadocJar.Empty()` publish an empty jar
      // - `JavadocJar.Javadoc()` to publish standard javadocs
      // the second whether to publish a sources jar
      configure(new JavaLibrary(new JavadocJar.Javadoc(), true))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.JavaLibrary
    import com.vanniktech.maven.publish.JavadocJar

    mavenPublishing {
      configure(JavaLibrary(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        javadocJar = JavadocJar.Javadoc(),
        // whether to publish a sources jar
        sourcesJar = true,
      ))
    }
    ```

## Kotlin JVM Library

For projects using the `org.jetbrains.kotlin.jvm` plugin.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.KotlinJvm
    import com.vanniktech.maven.publish.JavadocJar

    mavenPublishing {
      // the first parameter configures the -javadoc artifact, possible values:
      // - `JavadocJar.None()` don't publish this artifact
      // - `JavadocJar.Empty()` publish an empty jar
      // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
      // the second whether to publish a sources jar
      configure(new KotlinJvm(new JavadocJar.Dokka("dokkaHtml"), true))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.KotlinJvm
    import com.vanniktech.maven.publish.JavadocJar

    mavenPublishing {
      configure(KotlinJvm(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Dokka("dokkaHtml"),
        // whether to publish a sources jar
        sourcesJar = true,
      ))
    }
    ```

## Kotlin Multiplatform Library

For projects using the `org.jetbrains.kotlin.multiplatform` plugin.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.KotlinMultiplatform
    import com.vanniktech.maven.publish.JavadocJar

    mavenPublishing {
      // the parameter configures the -javadoc artifact, possible values:
      // - `JavadocJar.None()` don't publish this artifact
      // - `JavadocJar.Empty()` publish an empty jar
      // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
      // the second whether to publish a sources jar
      // the third parameters configures which Android library variants to publish if this project has an Android target
      // defaults to "release" when using the main plugin and nothing for the base plugin
      configure(new KotlinMultiplatform(new JavadocJar.Dokka("dokkaHtml"), true, ["debug", "release"]))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.KotlinMultiplatform
    import com.vanniktech.maven.publish.JavadocJar

    mavenPublishing {
      // sources publishing is always enabled by the Kotlin Multiplatform plugin
      configure(KotlinMultiplatform(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Dokka("dokkaHtml"),
        // whether to publish a sources jar
        sourcesJar = true,
        // configure which Android library variants to publish if this project has an Android target
        // defaults to "release" when using the main plugin and nothing for the base plugin
        androidVariantsToPublish = listOf("debug", "release"),
      ))
    }
    ```

## Gradle Plugin

For projects using the `java-gradle-plugin` plugin. When also using `com.gradle.plugin-publish` please
use [GradlePublishPlugin](#gradle-publish-plugin)

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.GradlePlugin
    import com.vanniktech.maven.publish.JavadocJar

    mavenPublishing {
      // the first parameter configures the -javadoc artifact, possible values:
      // - `JavadocJar.None()` don't publish this artifact
      // - `JavadocJar.Empty()` publish an empty jar
      // - `JavadocJar.Javadoc()` to publish standard javadocs
      // the second whether to publish a sources jar
      configure(new GradlePlugin(new JavadocJar.Javadoc(), true))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.GradlePlugin
    import com.vanniktech.maven.publish.JavadocJar

    mavenPublishing {
      configure(GradlePlugin(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Javadoc(),
        // whether to publish a sources jar
        sourcesJar = true,
      ))
    }
    ```

## Gradle Publish Plugin

For projects using the `com.gradle.plugin-publish` plugin. This will always publish a sources jar
and a javadoc jar.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.GradlePublishPlugin

    mavenPublishing {
      configure(new GradlePublishPlugin())
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.GradlePublishPlugin

    mavenPublishing {
      configure(GradlePublishPlugin())
    }
    ```

## Java Platform


For projects using the `java-platform` plugin.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.JavaPlatform

    mavenPublishing {
      configure(new JavaPlatform())
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.JavaPlatform

    mavenPublishing {
      configure(JavaPlatform())
    }
    ```


## Version Catalog


For projects using the `version-catalog` plugin.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.VersionCatalog

    mavenPublishing {
      configure(new VersionCatalog())
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.VersionCatalog

    mavenPublishing {
      configure(VersionCatalog())
    }
    ```
