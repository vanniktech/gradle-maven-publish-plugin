# Configuring what to publish

It is possible to configure publishing for the following Gradle plugins:

- `com.android.library` as [single variant library](#android-library-single-variant) or
  as [multi variant library](#android-library-multiple-variants)
- [`com.android.fused-library`](#android-fused-library)
- [`org.jetbrains.kotlin.jvm`](#kotlin-jvm-library)
- [`org.jetbrains.kotlin.multiplatform`](#kotlin-multiplatform-library)
    - automatically includes `com.android.kotlin.multiplatform.library`
- [`java`](#java-library)
- [`java-library`](#java-library)
- [`java-gradle-plugin`](#gradle-plugin)
- [`com.gradle.plugin-publish`](#gradle-publish-plugin)
- [`java-platform`](#java-platform)
- [`version-catalog`](#version-catalog)

## Automatic plugin selection

To automatically select what to publish based on already applied plugins call
`configureBasedOnAppliedPlugins(...)`. This is called automatically by the
`com.vanniktech.maven.publish` plugin, you only need to call this yourself when
using `com.vanniktech.maven.publish.base` or when you want to configure the
sources and javadoc jars that are being published.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configureBasedOnAppliedPlugins(
        // the first parameter configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        new JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        new SourcesJar.Sources()
      )
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configureBasedOnAppliedPlugins(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        sourcesJar = SourcesJar.Sources(),
      )
    }
    ```


## Android Library (multiple variants)

For projects using the `com.android.library` plugin. This will publish all variants of the project (e.g. both
`debug` and `release`) or a subset of specified variants.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.AndroidMultiVariantLibrary
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(new AndroidMultiVariantLibrary(
        // the first parameter configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        new JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        new SourcesJar.Sources()
      ))

      // or

      configure(new AndroidMultiVariantLibrary(
        // the first parameter configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        new JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        new SourcesJar.Sources(),
        // limit which build types to include
        ["beta", "release"] as Set
      ))

      // or

      configure(new AndroidMultiVariantLibrary(
        // the first parameter configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        new JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        new SourcesJar.Sources(),
        // limit which build types to include
        ["beta", "release"] as Set,
        // limit which flavors to include
        // the map key is a flavor dimension, the set contains the flavors
        ["store": ["google", "samsung"] as Set]
      ))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.AndroidMultiVariantLibrary
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(AndroidMultiVariantLibrary(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        sourcesJar = SourcesJar.Sources(),
      ))
      // or
      configure(AndroidMultiVariantLibrary(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        sourcesJar = SourcesJar.Sources(),
        // limit which build types to include
        includedBuildTypeValues = setOf("beta", "release"),
      ))
      // or
      configure(AndroidMultiVariantLibrary(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        sourcesJar = SourcesJar.Sources(),
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
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(new AndroidSingleVariantLibrary(
        // the first parameter configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        new JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        new SourcesJar.Sources(),
        // which variant is published
        "release"
      ))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(AndroidSingleVariantLibrary(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        sourcesJar = SourcesJar.Sources(),
        // the published variant
        variant = "release",
      ))
    }
    ```


## Android Fused Library

For projects using the `com.android.fused-library` plugin.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

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

    Signing is currently unsupported for Android fused libraries because of an
    [issue](https://issuetracker.google.com/issues/466503466) in the Android
    Gradle plugin.

!!! info

    Configuring source and javadoc publishing is currently not possible. For
    javadoc jars the plugin will always publish empty jars. For the sources jar
    the plugin will publish and empty jar for Android Gradle Plugin 8.x and a jar
    produced automatically by the Android Gradle Plugin on version 9.0 and newer.


## Java Library

For projects using the `java-library` plugin.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.JavaLibrary
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(new JavaLibrary(
        // the first parameter configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        new JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        new SourcesJar.Sources()
      ))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.JavaLibrary
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(JavaLibrary(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        javadocJar = JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        sourcesJar = SourcesJar.Sources(),
      ))
    }
    ```

## Kotlin JVM Library

For projects using the `org.jetbrains.kotlin.jvm` plugin.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.KotlinJvm
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(new KotlinJvm(
        // the first parameter configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        new JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        new SourcesJar.Sources()
      ))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.KotlinJvm
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(KotlinJvm(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        sourcesJar = SourcesJar.Sources(),
      ))
    }
    ```

## Kotlin Multiplatform Library

For projects using the `org.jetbrains.kotlin.multiplatform` plugin.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.KotlinMultiplatform
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(new KotlinMultiplatform(
        // the first parameter configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        new JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        new SourcesJar.Sources(),
        // configure which Android library variants to publish if this project has an Android target
        // defaults to "release" when using the main plugin and nothing for the base plugin
        ["debug", "release"]
      ))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.KotlinMultiplatform
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      // sources publishing is always enabled by the Kotlin Multiplatform plugin
      configure(KotlinMultiplatform(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        sourcesJar = SourcesJar.Sources(),
        // configure which Android library variants to publish if this project has an Android target
        // defaults to "release" when using the main plugin and nothing for the base plugin
        androidVariantsToPublish = listOf("debug", "release"),
      ))
    }
    ```

!!! info

    The `com.android.kotlin.multiplatform.library` plugin does not need any special configuration. When using it
    leave out the third parameter (`androidVariantsToPublish`) of `KotlinMultiplatform`.

## Gradle Plugin

For projects using the `java-gradle-plugin` plugin. When also using `com.gradle.plugin-publish` please
use [GradlePublishPlugin](#gradle-publish-plugin)

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.GradlePlugin
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(new GradlePlugin(
        // the first parameter configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        new JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        new SourcesJar.Sources()
      ))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.GradlePlugin
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(GradlePlugin(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Empty(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        sourcesJar = SourcesJar.Sources(),
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
