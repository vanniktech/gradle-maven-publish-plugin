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

## Javadoc and Sources

Most platforms allow configuring how the Javadoc and Sources jars are generated.

### [JavadocJar][JavadocJar]

- `JavadocJar.None()`: Do not create a Javadoc jar. This option is not compatible with Maven Central.
- `JavadocJar.Empty()`: Creates an empty javadoc jar to satisfy Maven Central requirements.
- `JavadocJar.Javadoc()`: Creates a regular javadoc jar using Gradle's default `javadoc` task.
- `JavadocJar.Dokka("dokkaJavadoc")`: Creates a Javadoc jar using Dokka's output. The argument is the name of the dokka task that should be used.

### [SourcesJar][SourcesJar]

- `SourcesJar.None`: Do not create a sources jar. This option is not compatible with Maven Central.
- `SourcesJar.Empty`: Creates an empty sources jar to satisfy Maven Central requirements.
- `SourcesJar.Sources`: Creates a regular sources jar using Gradle's default `sourcesJar` task.

## Android Library (multiple variants)

For projects using the `com.android.library` plugin. This will publish all variants of the project (e.g. both
`debug` and `release`) or a subset of specified variants.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.AndroidMultiVariantLibrary
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(
        // there are multiple overloads for the constructor
        new AndroidMultiVariantLibrary(
          new JavadocJar.Empty(),
          SourcesJar.Sources.INSTANCE,
          // which build types to include
          ["beta", "release"] as Set,
          // which flavors to include, the map key is a flavor dimension, the set contains the flavors
          ["store": ["google", "samsung"] as Set],
        )
      )
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.AndroidMultiVariantLibrary
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(
        // there are multiple overloads for the constructor
        AndroidMultiVariantLibrary(
          javadocJar = JavadocJar.Empty(),
          sourcesJar = SourcesJar.Sources,
          // which build types to include
          includedBuildTypeValues = setOf("beta", "release"),
          // which flavors to include, the map key is a flavor dimension, the set contains the flavors
          includedFlavorDimensionsAndValues = mapOf("store" to setOf("google", "samsung")),
        )
      )
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
      configure(
        // there are multiple overloads for the constructor
        new AndroidSingleVariantLibrary(
          new JavadocJar.Empty(),
          SourcesJar.Sources.INSTANCE,
          // which variant is published
          "release",
        )
      )
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(
        // there are multiple overloads for the constructor
        AndroidSingleVariantLibrary(
          javadocJar = JavadocJar.Empty(),
          sourcesJar = SourcesJar.Sources,
          // which variant is published
          variant = "release",
        )
      )
    }
    ```


## Android Fused Library

For projects using the `com.android.fused-library` plugin.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.AndroidFusedLibrary

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
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(
        new JavaLibrary(
          new JavadocJar.Empty(),
          SourcesJar.Sources.INSTANCE,
        )
      )
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.JavaLibrary
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(
        JavaLibrary(
          javadocJar = JavadocJar.Empty(),
          sourcesJar = SourcesJar.Sources,
        )
      )
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
      configure(
        new KotlinJvm(
          new JavadocJar.Empty(),
          SourcesJar.Sources.INSTANCE,
        )
      )
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.KotlinJvm
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(
        KotlinJvm(
          javadocJar = JavadocJar.Empty(),
          sourcesJar = SourcesJar.Sources,
        )
      )
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
      configure(
        // there are multiple overloads for the constructor
        new KotlinMultiplatform(
          new JavadocJar.Empty(),
          SourcesJar.Sources.INSTANCE,
          // which Android library variants to publish if this project has an Android target
          ["release"],
        )
      )
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.KotlinMultiplatform
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      // there are multiple overloads for the constructor
      configure(
        KotlinMultiplatform(
          javadocJar = JavadocJar.Empty(),
          sourcesJar = SourcesJar.Sources,
          // which Android library variants to publish if this project has an Android target
          androidVariantsToPublish = listOf("release"),
        )
      )
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
      configure(
        new GradlePlugin(
          new JavadocJar.Empty(),
          SourcesJar.Sources.INSTANCE,
        )
      )
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.GradlePlugin
    import com.vanniktech.maven.publish.JavadocJar
    import com.vanniktech.maven.publish.SourcesJar

    mavenPublishing {
      configure(
        GradlePlugin(
          javadocJar = JavadocJar.Empty(),
          sourcesJar = SourcesJar.Sources,
        )
      )
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



[JavadocJar]: api/plugin/com.vanniktech.maven.publish/-javadoc-jar/index.html
[SourcesJar]: api/plugin/com.vanniktech.maven.publish/-sources-jar/index.html
