# Other Maven Repositories

This describes how to configure the plugin to publish to arbitrary Maven repositories. For publishing open source
projects see [Maven Central](central.md).

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

## Configuring the repository

A new repository to publish to can be added like this

=== "build.gradle"

    ```groovy
    publishing {
        repositories {
            maven {
                name = "myRepo"
                url = layout.buildDirectory.dir('repo')
                // or
                url = "http://my.org/repo"
                // or when a separate snapshot repository is required
                url = version.toString().endsWith("SNAPSHOT") ? "http://my.org/repos/snapshots" : "http://my.org/repos/releases"
            }

            // more repositories can go here
        }
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    publishing {
        repositories {
            maven {
                name = "myRepo"
                url = uri(layout.buildDirectory.dir("repo"))
                // or
                url = uri("http://my.org/repo")
                // or when a separate snapshot repository is required
                url = uri(if (version.toString().endsWith("SNAPSHOT")) "http://my.org/repos/snapshots" else "http://my.org/repos/releases")
            }

            // more repositories can go here
        }
    }
    ```

### Github Packages example

=== "build.gradle"

    ```groovy
    publishing {
        repositories {
            maven {
                name = "githubPackages"
                url = "https://maven.pkg.github.com/your-org/your-project"
                // username and password (a personal Github access token) should be specified as
                // `githubPackagesUsername` and `githubPackagesPassword` Gradle properties or alternatively
                // as `ORG_GRADLE_PROJECT_githubPackagesUsername` and `ORG_GRADLE_PROJECT_githubPackagesPassword`
                // environment variables
                credentials(PasswordCredentials)
            }
        }
    }
    ```

=== "build.gradle.kts"

    ```kotlin
    publishing {
        repositories {
            maven {
                name = "githubPackages"
                url = uri("https://maven.pkg.github.com/your-org/your-project")
                // username and password (a personal Github access token) should be specified as
                // `githubPackagesUsername` and `githubPackagesPassword` Gradle properties or alternatively
                // as `ORG_GRADLE_PROJECT_githubPackagesUsername` and `ORG_GRADLE_PROJECT_githubPackagesPassword`
                // environment variables
                credentials(PasswordCredentials::class)
            }
        }
    }
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

      // the following is optional

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
      coordinates("com.example.mylibrary", "library-name", "1.0.3-SNAPSHOT")

      // the following is optional

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

    # the following is optional

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

## Publishing

To publish the project run
```
./gradlew publishAllPublicationsTo<RepoName>Repository
```

`<RepoName>` refers to the name used in the [configuring the repository section](#configuring-the-repository)
and would be `MyRepo` for the example there.
