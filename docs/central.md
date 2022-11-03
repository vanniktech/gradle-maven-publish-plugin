# Maven Central

This describes how to configure the plugin to publish to [Maven Central](https://search.maven.org/)
the most common repository for opensource projects.

## Prerequisites

1. An account for oss.sonatype.org or s01.oss.sonatype.org
   1. Create an account in Sonatype's Jira [here](https://issues.sonatype.org/secure/Signup!default.jspa)
   2. [Choose the coordinates]( https://central.sonatype.org/publish/requirements/coordinates/)
   3. When using a personal domain as groupId [create a TXT record](https://central.sonatype.org/faq/how-to-set-txt-record)
   4. [Create a ticket](https://issues.sonatype.org/secure/CreateIssue.jspa?pid=10134&issuetype=21)
2. A GPG key that can be used to sign artifacts
   1. [Create a GPG key pair](https://central.sonatype.org/publish/requirements/gpg/#generating-a-key-pair)
   2. [Distribute the public key](https://central.sonatype.org/publish/requirements/gpg/#distributing-your-public-key)

## Applying the plugin

Add the plugin to any Gradle project that should be published

=== "build.gradle"

    ```groovy
    plugins {
      id "com.vanniktech.maven.publish" version "0.22.0"
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    plugins {
      id("com.vanniktech.maven.publish") version "0.22.0"
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

      signAllPublications()
    }
    ```

=== "gradle.properties"

    ```properties
    SONATYPE_HOST=DEFAULT
    # or when publishing to https://s01.oss.sonatype.org
    SONATYPE_HOST=S01

    RELEASE_SIGNING_ENABLED=true
    ```

## Configuring the POM

The pom is published alongside the project and contains the project coordinates
as well as some general information about the project like an url and the used
license.

This configuration also determines the coordinates (`group:artifactId:version`) used to consume the library.

=== "build.gradle"

    ```groovy
    group = "com.example.project"
    version = "1.0.3-SNAPSHOT"
    // note that it's currently not possible to modify the artifact id through the DSL
    // by default `project.name` is used, to modify it use gradle.properties

    mavenPublishing {
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
    group = "com.example.project"
    version = "1.0.3-SNAPSHOT"
    // note that it's currently not possible to modify the artifact id through the DSL
    // by default `project.name` is used, to modify it use gradle.properties

    mavenPublishing {
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
    VERSION_NAME=3.0.5

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

For the publishing to work the credentials for Sonatype OSS as well as for the
GPG key that is used for signing need to provided. To keep them out of version
control it is recommended to either put this into the `gradle.properties` file
user home or to use environment variables for publishing from CI servers.

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

### In memory GPG key

To obtain the in memory signing key run the following command. **Warning: this will print the private
GPG key in plain text**
```sh
gpg2 --export-secret-keys --armor <key id> <path to secring.gpg> | grep -v '\-\-' | grep -v '^=.' | tr -d '\n'
```
The result will be a very long single line string that looks like this
```
lQdGBF4jUfwBEACblZV4uBViHcYLOb2280tEpr64iB9b6YRkWil3EODiiLd9JS3V...9pip+B1QLwEdLCEJA+3IIiw4qM5hnMw=
```

## Publishing

The publishing process for Maven Central consists of several steps

1. A staging repository is created on Sonatype OSS
2. The artifacts are uploaded to this staging repository
3. The staging repository is closed
4. The staging repository is released
5. All artifacts in the released repository will be synchronized to maven central

By running the following Gradle task the plugin will take care of steps 1 to 3 automatically:

```
./gradlew publishAllPublicationsToMavenCentral
```

The releasing step can be done manually by going to oss.sonatype.org (or s01.oss.sonatype.org) and
clicking the button in the web UI or by executing `./gradlew closeAndReleaseRepository`.

It is also possible to configure the plugin to perform this step automatically together with the first 3
by adding an extra parameter in the DSL or setting a Gradle property

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
      publishToMavenCentral(SonatypeHost.DEFAULT, true)
      // or when publishing to https://s01.oss.sonatype.org
      publishToMavenCentral(SonatypeHost.S01, true)
    }
    ```

=== "gradle.properties"

    ```properties
    SONATYPE_AUTOMATIC_RELEASE=true
    ```

Regardless of whether it is done automatically or manually after the staging repository is released the artifacts
will be synced to Maven Central. This process takes 10-30 minutes and when it is completed
the artifacts are available for download.
