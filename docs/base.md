# Base plugin

Starting with version 0.15.0 there is a base plugin. This new plugin has the same capabilities as the main
plugin but does not configure anything automatically. In the current stage the APIs are still marked with `@Incubating`
so they might change.

## Applying the plugin

Add the plugin to any Gradle project that should be published

=== "build.gradle"

    ```groovy
    plugins {
      id "com.vanniktech.maven.publish.base" version "0.23.1"
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    plugins {
      id("com.vanniktech.maven.publish.base") version "0.23.1"
    }
    ```

## General configuration


Follow the steps of the [Maven Central](central.md) or [other Maven repositories](other.md) guides. Note that the
configuration of where to publish to with Gradle properties won't work in the base plugin. For the pom configuration
via Gradle properties the following needs to be enabled in the DSL:

=== "build.gradle"

    ```groovy
    mavenPublishing {
      pomFromGradleProperties()
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    mavenPublishing {
      pomFromGradleProperties()
    }
    ```

## Configuring what to publish

The biggest difference between the regular and the base plugin is that it won't setup what to publish by default. The
base plugin offers various APIs depending on the type of project that should be published

### Android Library (multiple variants)

For projects using the `com.android.library` plugin. This will publish all variants of the project (e.g. both
`debug` and `release`) or a subset of specified variants.

=== "build.gradle"

    ```groovy
    mavenPublishing {
      // the first parameter represennts whether to publish a sources jar
      // the second whether to publish a javadoc jar
      configure(new AndroidMultiVariantLibrary(true, true))
      // or to limit which build types to include
      configure(new AndroidMultiVariantLibrary(true, true, ["beta", "release"]))
      // or to limit which flavors to include, the map key is a flavor dimension, the set contains the flavors
      configure(new AndroidMultiVariantLibrary(true, true, ["beta", "release"], ["store": ["google", "samsung"]]))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
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

### Android Library (single variant)

For projects using the `com.android.library` plugin. Compared to the multi variant version above this will only publish
the specified variant instead of publishing all of them.

=== "build.gradle"

    ```groovy
    mavenPublishing {
      // the first parameter represennts which variant is published
      // the second whether to publish a sources jar
      // the third whether to publish a javadoc jar
      configure(new AndroidSingleVariantLibrary("release", true, true))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
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

### Java Library

For projects using the `java-library` plugin.

=== "build.gradle"

    ```groovy
    mavenPublishing {
      // the first parameter configures the -javadoc artifact, possible values:
      // - `JavadocJar.None()` don't publish this artifact
      // - `JavadocJar.Empty()` publish an emprt jar
      // - `JavadocJar.Javadoc()` to publish standard javadocs
      // the second whether to publish a sources jar
      configure(new JavaLibrary(new JavadocJar.Javadoc(), true))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    mavenPublishing {
      configure(JavaLibrary(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an emprt jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        javadocJar = JavadocJar.Javadoc(),
        // whether to publish a sources jar
        sourcesJar = true,
      ))
    }
    ```

### Kotlin JVM Library

For projects using the `org.jetbrains.kotlin.jvm` plugin.

=== "build.gradle"

    ```groovy
    mavenPublishing {
      // the first parameter configures the -javadoc artifact, possible values:
      // - `JavadocJar.None()` don't publish this artifact
      // - `JavadocJar.Empty()` publish an emprt jar
      // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
      // the second whether to publish a sources jar
      configure(new KotlinJvm(new JavadocJar.Dokka("dokkaHtml"), true))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    mavenPublishing {
      configure(KotlinJvm(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an emprt jar
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Dokka("dokkaHtml"),
        // whether to publish a sources jar
        sourcesJar = true,
      ))
    }
    ```

### Kotlin JS Library

For projects using the `org.jetbrains.kotlin.js` plugin.

=== "build.gradle"

    ```groovy
    mavenPublishing {
      // the first parameter configures the -javadoc artifact, possible values:
      // - `JavadocJar.None()` don't publish this artifact
      // - `JavadocJar.Empty()` publish an emprt jar
      // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
      // the second whether to publish a sources jar
      configure(new KotlinJs(new JavadocJar.Dokka("dokkaHtml"), true))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    mavenPublishing {
      configure(KotlinJs(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an emprt jar
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Dokka("dokkaHtml"),
        // whether to publish a sources jar
        sourcesJar = true,
      ))
    }
    ```

### Kotlin Multiplatform Library

For projects using the `org.jetbrains.kotlin.multiplatform` plugin.

=== "build.gradle"

    ```groovy
    mavenPublishing {
      // the parameter configures the -javadoc artifact, possible values:
      // - `JavadocJar.None()` don't publish this artifact
      // - `JavadocJar.Empty()` publish an emprt jar
      // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
      // sources publishing is always enabled by the Kotlin Multiplatform plugin
      configure(new KotlinMultiplatform(new JavadocJar.Dokka("dokkaHtml")))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    mavenPublishing {
      // sources publishing is always enabled by the Kotlin Multiplatform plugin
      configure(KotlinMultiplatform(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an emprt jar
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Dokka("dokkaHtml"),
      ))
    }
    ```

### Gradle Plugin


For projects using the `java-gradle-plugin` plugin.

=== "build.gradle"

    ```groovy
    mavenPublishing {
      // the first parameter configures the -javadoc artifact, possible values:
      // - `JavadocJar.None()` don't publish this artifact
      // - `JavadocJar.Empty()` publish an emprt jar
      // - `JavadocJar.Javadoc()` to publish standard javadocs
      // the second whether to publish a sources jar
      configure(new GradlePlugin(new JavadocJar.Javadoc(), true))
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    mavenPublishing {
      configure(GradlePlugin(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an emprt jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Javadoc(),
        // whether to publish a sources jar
        sourcesJar = true,
      ))
    }
    ```

### Java Platform


For projects using the `java-platform` plugin.

=== "build.gradle"

    ```groovy
    mavenPublishing {
      configure(new JavaPlatform())
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    mavenPublishing {
      configure(JavaPlatform())
    }
    ```


### Version Catalog


For projects using the `version-catalog` plugin.

=== "build.gradle"

    ```groovy
    mavenPublishing {
      configure(new VersionCatalog())
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    mavenPublishing {
      configure(VersionCatalog())
    }
    ```
