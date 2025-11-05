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

## Configuring Maven Central

After applying the plugin the first step is to enable publishing to Maven Central
is to add it is as a target and enable GPG signing which is a Central requirement.
This can be done through either the DSL or by setting Gradle properties.

=== "build.gradle"

    ```groovy
    import com.vanniktech.maven.publish.SonatypeHost

    mavenPublishing {
      publishToMavenCentral()

      signAllPublications()
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    import com.vanniktech.maven.publish.SonatypeHost

    mavenPublishing {
      publishToMavenCentral()

      signAllPublications()
    }
    ```

=== "gradle.properties"

    ```properties
    mavenCentralPublishing=true

    signAllPublications=true
    ```

## Configuring the POM

The pom is published alongside the project and contains the project coordinates
as well as some general information about the project like a url and the used
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

!!! warning

    It's discouraged to set `packaging` on the POM, since it can lead to errors
    or unintended behavior. The value will be automatically set based on the
    project type if needed.

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
requires a username/password that was generated via user tokens. The user token needs to be obtained on the [Central Portal](https://central.sonatype.org/publish/generate-portal-token/).

### In memory GPG key

To obtain the in memory signing key run the following command.

!!! warning

    This will print the private GPG key in plain text.

```sh
gpg --export-secret-keys --armor <key id>
```

!!! info

    If you have a `secring.gpg` file that contains your key add the path to that
    file after the `<key id>`:
    ```sh
    gpg --export-secret-keys --armor <key id>  <path to secring.gpg>
    ```

The result will be a long, multi-line string that looks like this
```
-----BEGIN PGP PRIVATE KEY BLOCK-----

lQdGBF4jUfwBEACblZV4uBViHcYLOb2280tEp
r64iB9b6YRkWil3EODiiLd9JS3V9pip+B1QLw
...
EdLCEJA+3IIiw4qM5hnMw=
=s83f
-----END PGP PRIVATE KEY BLOCK-----
```

Make sure to copy this string in its entirety.

## Publishing snapshots

Snapshots can be published by setting the version to something ending with `-SNAPSHOT`
and then running the following Gradle task:

```
./gradlew publishToMavenCentral
```

The snapshot will be automatically available in the [Central Portal snapshot repository](https://central.sonatype.com/repository/maven-snapshots/) directly
after the task finished.

Signing is not required for snapshot builds, but if the configuration is present the build
will still be signed.


## Publishing releases

The publishing process for Maven Central consists of several steps

| Step | Who            | What                                           |
|------|----------------|------------------------------------------------|
| 1    | Plugin         | The artifacts are uploaded to a new deployment |
| 2    | Central Portal | The deployment is validated |
| 3    | You or plugin  | The deployment is published to Maven Central |
| 4    | Central Portal | The published artifacts are available in Maven Central |

!!! note

    Step 4 can take 10 to 30 minutes and only after it completed the published
    artifacts will be available for download.

### Uploading with manual publishing

Run the following Gradle task:
```sh
./gradlew publishToMavenCentral
```

Afterward go to [Deployments on the Central Portal website](https://central.sonatype.com/publishing/deployments)
and click "Publish" on the deployment.

### Uploading with automatic publishing

For automatic publishing use one of the following options

=== "Command"

    Instead of running `publishToMavenCentral` as described above use:
    ```sh
    ./gradlew publishAndReleaseToMavenCentral
    ```

=== "build.gradle"

    When calling `publishToMavenCentral` in the DSL add `true` as a parameter.
    ```groovy
    mavenPublishing {
      publishToMavenCentral(true)

      // rest of publishing config
    }
    ```

    To publish use
    ```sh
    ./gradlew publishToMavenCentral
    ```

=== "build.gradle.kts"

    When calling `publishToMavenCentral` in the DSL add `automaticRelease = true` as a parameter to
    make any publish task also take care of step 3.
    ```properties
    mavenPublishing {
      publishToMavenCentral(automaticRelease = true)

      // rest of publishing config
    }
    ```

    To publish use
    ```sh
    ./gradlew publishToMavenCentral
    ```

=== "gradle.properties"

    Add the following to `gradle.properties` to make any publish task also take care of
    step 3.
    ```properties
    mavenCentralAutomaticPublishing=true
    ```

    To publish use
    ```sh
    ./gradlew publishToMavenCentral
    ```

### Deployment validation

When automatic releases are enabled, the plugin automatically monitors the deployment status until it reaches
a terminal state (`PUBLISHED` or `FAILED`).

The validation process:

- Polls the deployment status every 5 seconds (configurable via `SONATYPE_POLL_INTERVAL_SECONDS` property)
- Only stops when reaching a terminal state (`PUBLISHED` or `FAILED`)
- Times out after 15 minutes by default (configurable via `SONATYPE_CLOSE_TIMEOUT_SECONDS` property)
- Fails the build if the deployment enters the `FAILED` state, displaying validation errors

#### Disabling automatic validation

Deployment validation for automatic releases is enabled by default. If you prefer to check validation status
manually, you can disable automatic validation:

=== "build.gradle"

    ```groovy
    mavenPublishing {
      publishToMavenCentral(true, false) // automaticRelease = true, validateDeployment = false

      // rest of publishing config
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    mavenPublishing {
      publishToMavenCentral(automaticRelease = true, validateDeployment = false)

      // rest of publishing config
    }
    ```

=== "gradle.properties"

    ```properties
    mavenCentralDeploymentValidation=false
    ```

When validation is disabled, the build will complete immediately after uploading the artifacts,
and you can check the deployment status manually on
the [Central Portal website](https://central.sonatype.com/publishing/deployments).
