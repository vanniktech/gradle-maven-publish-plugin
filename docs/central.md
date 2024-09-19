# Maven Central

This describes how to configure the plugin to publish to [Maven Central](https://central.sonatype.com/)
the most common repository for opensource projects.

## Prerequisites

1. A [Central Portal account](https://central.sonatype.org/register/central-portal/#create-an-account)
2. A [registered namespace](https://central.sonatype.org/register/namespace/)
3. A GPG key that can be used to sign artifacts
    1. [Create a GPG key pair](https://central.sonatype.org/publish/requirements/gpg/#generating-a-key-pair)
    2. [Distribute the public key](https://central.sonatype.org/publish/requirements/gpg/#distributing-your-public-key)

## Applying the plugin

Add the plugin to any Gradle project that should be published

=== "build.gradle"

    ```groovy
    plugins {
      id "com.vanniktech.maven.publish" version "<latest-version>"
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    plugins {
      id("com.vanniktech.maven.publish") version "<latest-version>"
    }
    ```

## Configuring what to publish

By default, the plugin will automatically detect other applied plugins like the
Android Gradle plugin or Kotlin Gradle plugin and set up what to publish automatically.
This automatic configuration includes publishing a sources and a javadoc jar. The
javadoc jar content is either created from the default javadoc task or from Dokka if
applied.

To modify these defaults it is possible to call `configure` in the DSL. For
more check out the [what to publish page](what.md) which contains a detailed
description of available options for each project type.

=== "build.gradle"

    ```groovy
    mavenPublishing {
      configure(...)
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    mavenPublishing {
      configure(...)
    }
    ```

## Configuring Maven Central

After applying the plugin the first step is to enable publishing to Maven Central
is to add it is as a target and enable GPG signing which is a Central requirement.
This can be done through either the DSL or by setting Gradle properties.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.SonatypeHost

    mavenPublishing {
      publishToMavenCentral(SonatypeHost.DEFAULT)
      // or when publishing to https://s01.oss.sonatype.org
      publishToMavenCentral(SonatypeHost.S01)
      // or when publishing to https://central.sonatype.com/
      publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

      signAllPublications()
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.SonatypeHost

    mavenPublishing {
      publishToMavenCentral(SonatypeHost.DEFAULT)
      // or when publishing to https://s01.oss.sonatype.org
      publishToMavenCentral(SonatypeHost.S01)
      // or when publishing to https://central.sonatype.com/
      publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

      signAllPublications()
    }
    ```

=== "gradle.properties"

    ```properties
    SONATYPE_HOST=DEFAULT
    # or when publishing to https://s01.oss.sonatype.org
    SONATYPE_HOST=S01
    // or when publishing to https://central.sonatype.com/
    SONATYPE_HOST=CENTRAL_PORTAL

    RELEASE_SIGNING_ENABLED=true
    ```

## Configuring the POM

The pom is published alongside the project and contains the project coordinates
as well as some general information about the project like an url and the used
license.

This configuration also determines the coordinates (`group:artifactId:version`) used to consume the library.

=== "build.gradle"

    ```groovy
    mavenPublishing {
      coordinates("com.example.mylibrary", "library-name", "1.0.3-SNAPSHOT")

      pom {
        name = "My Library"
        description = "A description of what my library does."
        inceptionYear = "2020"
        url = "https://github.com/username/mylibrary/"
        licenses {
          license {
            name = "The Apache License, Version 2.0"
            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            distribution = "http://www.apache.org/licenses/LICENSE-2.0.txt"
          }
        }
        developers {
          developer {
            id = "username"
            name = "User Name"
            url = "https://github.com/username/"
          }
        }
        scm {
          url = "https://github.com/username/mylibrary/"
          connection = "scm:git:git://github.com/username/mylibrary.git"
          developerConnection = "scm:git:ssh://git@github.com/username/mylibrary.git"
        }
      }
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    mavenPublishing {
      coordinates("com.example.mylibrary", "mylibrary-runtime", "1.0.3-SNAPSHOT")

      pom {
        name.set("My Library")
        description.set("A description of what my library does.")
        inceptionYear.set("2020")
        url.set("https://github.com/username/mylibrary/")
        licenses {
          license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
          }
        }
        developers {
          developer {
            id.set("username")
            name.set("User Name")
            url.set("https://github.com/username/")
          }
        }
        scm {
          url.set("https://github.com/username/mylibrary/")
          connection.set("scm:git:git://github.com/username/mylibrary.git")
          developerConnection.set("scm:git:ssh://git@github.com/username/mylibrary.git")
        }
      }
    }
    ```

=== "gradle.properties"

    ```properties
    GROUP=com.test.mylibrary
    POM_ARTIFACT_ID=mylibrary-runtime
    VERSION_NAME=1.0.3-SNAPSHOT

    POM_NAME=My Library
    POM_DESCRIPTION=A description of what my library does.
    POM_INCEPTION_YEAR=2020
    POM_URL=https://github.com/username/mylibrary/

    POM_LICENSE_NAME=The Apache Software License, Version 2.0
    POM_LICENSE_URL=https://www.apache.org/licenses/LICENSE-2.0.txt
    POM_LICENSE_DIST=repo

    POM_SCM_URL=https://github.com/username/mylibrary/
    POM_SCM_CONNECTION=scm:git:git://github.com/username/mylibrary.git
    POM_SCM_DEV_CONNECTION=scm:git:ssh://git@github.com/username/mylibrary.git

    POM_DEVELOPER_ID=username
    POM_DEVELOPER_NAME=User Name
    POM_DEVELOPER_URL=https://github.com/username/
    ```

## Secrets

When publishing, you need to provide your Sonatype OSS credentials and signing GPG key.
To keep them out of version control, it is recommended to either put them in your user's
home `gradle.properties` file or to use environment variables (when publishing from CI servers).

=== "~/.gradle/gradle.properties"

    ```properties
    mavenCentralUsername=username
    mavenCentralPassword=the_password

    signing.keyId=12345678
    signing.password=some_password
    signing.secretKeyRingFile=/Users/yourusername/.gnupg/secring.gpg
    ```

=== "Environment variables"

    ```sh
    ORG_GRADLE_PROJECT_mavenCentralUsername=username
    ORG_GRADLE_PROJECT_mavenCentralPassword=the_password

    # see below for how to obtain this
    ORG_GRADLE_PROJECT_signingInMemoryKey=exported_ascii_armored_key
    # Optional
    ORG_GRADLE_PROJECT_signingInMemoryKeyId=12345678
    # If key was created with a password.
    ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=some_password
    ```

Note that the username/password here is *not* the same one you use to login; Sonatype publishing
requires a username/password that was [generated via user tokens](https://central.sonatype.org/publish/generate-portal-token/).

### In memory GPG key

To obtain the in memory signing key run the following command.

!!! warning

    This will print the private GPG key in plain text.

```sh
gpg --export-secret-keys --armor <key id> | grep -v '\-\-' | grep -v '^=.' | tr -d '\n'
```

!!! info

    If you have a `secring.gpg` file that contains your key add the path to that
    file after the `<key id>`:
    ```sh
    gpg --export-secret-keys --armor <key id>  <path to secring.gpg> | grep -v '\-\-' | grep -v '^=.' | tr -d '\n'
    ```

The result will be a very long single line string that looks like this
```
lQdGBF4jUfwBEACblZV4uBViHcYLOb2280tEpr64iB9b6YRkWil3EODiiLd9JS3V...9pip+B1QLwEdLCEJA+3IIiw4qM5hnMw=
```

## Publishing snapshots

!!! warning "Central Portal"

    Publishing snapshots is not supported when using the Central Portal (central.sonatype.com).


Snapshots can be published by setting the version to something ending with `-SNAPSHOT`
and then running the following Gradle task:

```
./gradlew publishAllPublicationsToMavenCentralRepository
```

The snapshot will be automatically available in Sonatype's
[snapshot repository](https://oss.sonatype.org/content/repositories/snapshots/) (or the
[S01 snapshot repository](https://s01.oss.sonatype.org/content/repositories/snapshots/)) directly after the
task finished.

Signing is not required for snapshot builds, but if the configuration is present the build
will still be signed.


## Publishing releases

The publishing process for Maven Central consists of several steps

1. A staging repository is created on Sonatype OSS
2. The artifacts are uploaded/published to this staging repository
3. The staging repository is closed
4. The staging repository is released
5. All artifacts in the released repository will be synchronized to maven central

The plugin will always do steps 1 to 3. Step 4 is only taken care of if automatic releases are enabled.

After the staging repository has been released, either manually or automatically, the artifacts will
be synced to Maven Central. This process usually takes around 10-30 minutes and only when it completes
the artifacts are available for download.

### Automatic release

Run the following task to let the plugin handle all steps automatically:

```
./gradlew publishAndReleaseToMavenCentral --no-configuration-cache
```

!!! note "Configuration cache"

    Configuration caching when uploading releases is currently not possible. Supporting it is
    blocked by [Gradle issue #22779](https://github.com/gradle/gradle/issues/22779).

It is possible to permanently enable the automatic releases so that regular publishing tasks
like `publish` and `publishToMavenCentral` will also always do the release step:

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.SonatypeHost

    mavenPublishing {
      publishToMavenCentral(SonatypeHost.DEFAULT, true)
      // or when publishing to https://s01.oss.sonatype.org
      publishToMavenCentral(SonatypeHost.S01, true)
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.SonatypeHost

    mavenPublishing {
      publishToMavenCentral(SonatypeHost.DEFAULT, automaticRelease = true)
      // or when publishing to https://s01.oss.sonatype.org
      publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)
    }
    ```

=== "gradle.properties"

    ```properties
    SONATYPE_AUTOMATIC_RELEASE=true
    ```

### Manual release

The release (step 4) can be done manually by running the following command, so that the plugin will
only do step 1 to 3:
```
./gradlew publishToMavenCentral --no-configuration-cache
```

!!! note "Configuration cache"

    Configuration caching when uploading releases is currently not possible. Supporting it is
    blocked by [Gradle issue #22779](https://github.com/gradle/gradle/issues/22779).

### Timeouts

From time to time Sonatype tends to time out during staging operations. The default timeouts of the plugin
are long already, but can be modified if needed. The timeout for HTTP requests can be modified with
`SONATYPE_CONNECT_TIMEOUT_SECONDS` which defaults to 1 minute. After a staging repository gets closed,
Sonatype will run several validations on it and the plugin needs to wait for those to finish, before it can
release the repository. The timeout for how long it is waiting for the close operation to finish can be
modified by `SONATYPE_CLOSE_TIMEOUT_SECONDS` and defaults to 15 minutes.

```properties
SONATYPE_CONNECT_TIMEOUT_SECONDS=60
SONATYPE_CLOSE_TIMEOUT_SECONDS=900
```
