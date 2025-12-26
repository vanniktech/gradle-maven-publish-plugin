# Base plugin

The base plugin generally provides the same functionality as the main plugin,
but it doesn't configure anything automatically and instead relies on manual
configuration.

## Applying the plugin

Add the plugin to any Gradle project that should be published

=== "build.gradle.kts"

    ```kotlin
    plugins {
      id("com.vanniktech.maven.publish.base") version "<latest-version>"
    }
    ```

=== "build.gradle"

    ```groovy
    plugins {
      id "com.vanniktech.maven.publish.base" version "<latest-version>"
    }
    ```

## General configuration

Follow the steps of the [Maven Central](central.md) or [other Maven repositories](other.md) guides.

!!! note

    The `mavenCentralPublishing`, `mavenCentralAutomaticPublishing` and `signAllPublications` properties are not
    considered by the base plugin and the appropriate DSL methods need to be called.

For the pom configuration via Gradle properties the following needs to be enabled in the DSL:

=== "build.gradle.kts"

    ```kotlin
    mavenPublishing {
      pomFromGradleProperties()
    }
    ```

=== "build.gradle"

    ```groovy
    mavenPublishing {
      pomFromGradleProperties()
    }
    ```

## Configuring what to publish

To define what should be published, the `configure` method in the `mavenPublishing`
block needs to be called. Check out the [what to publish page](what.md) which
contains a detailed description of available options for each project type.

It is also possible to get the automatic behavior of the main plugin by calling
`configureBasedOnAppliedPlugins()` instead of `configure`

=== "build.gradle.kts"

    ```kotlin
    mavenPublishing {
      configure(...)
      // or
      configureBasedOnAppliedPlugins()
    }
    ```

=== "build.gradle"

    ```groovy
    mavenPublishing {
      configure(...)
      // or
      configureBasedOnAppliedPlugins()
    }
    ```
