# gradle-maven-publish-plugin

Gradle plugin that creates a `publish` task to automatically upload all of your Java, Kotlin or Android
libraries to any Maven instance. This plugin is based on [Chris Banes initial implementation](https://github.com/chrisbanes/gradle-mvn-push)
and has been enhanced to add Kotlin support and keep up with the latest changes.

# Set up

### `build.gradle`

```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'com.vanniktech:gradle-maven-publish-plugin:0.15.1'
  }
}

apply plugin: "com.vanniktech.maven.publish"
```

Snapshots can be found [here](https://oss.sonatype.org/#nexus-search;quick~gradle-maven-publish-plugin).

### Setting properties

To configure the coordinates of your published artifacts as well as the POM this plugin
uses Gradle properties. It's generally recommended to set them in your `gradle.properties`
file.

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

This Gradle plugin is using itself to publish any of the updates and sets the Gradle properties in
this [gradle.properties](gradle.properties).

In multi module projects you can set most properties in the root `gradle.properties` file and
then only set the module specific ones in the submodules. For example if you have two modules
called `runtime` and `driver` you could only set `POM_ARTIFACT_ID` and `POM_NAME` in
`<project-dir>/runtime/gradle.properties` and `<project-dir>/driver/gradle.properties` while sharing
the rest by putting them into `<project-dir>/gradle.properties`.

### Where to upload to

Without any further configuration the plugin has two tasks. `publish` which will upload
to Maven Central (through Sonatype OSSRH) by default. To publish to the local maven repository on your
machine (`~/m2/repository`) there is `publishToMavenLocal`.

In case you are using `s01.oss.sonatype.org` you need to configure that like this:
```groovy
allprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        mavenPublish {
            sonatypeHost = "S01"
        }
    }
}
```

The username and password for Sonatype OSS can be provided as Gradle properties called `mavenCentralUsername`
and `mavenCentralPassword` to avoid having to commit them. You can also supply them as environment variables
called `ORG_GRADLE_PROJECT_mavenCentralUsername` and `ORG_GRADLE_PROJECT_mavenCentralPassword`.


You can add additional repositories to publish to using the standard Gradle APIs:

```groovy
publishing {
    repositories {
        maven {
            def releasesRepoUrl = "$buildDir/repos/releases"
            def snapshotsRepoUrl = "$buildDir/repos/snapshots"
            url = version.endsWith('SNAPSHOT') ? snapshotsRepoUrl : releasesRepoUrl
        }
    }
}
```

More information can be found in [Gradle's documentation](https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:repositories)

__Note:__ To prevent looping behavior, especially in Kotlin projects / modules, you need to run the `publish` task with `--no-daemon`and `--no-parallel` flags
### Signing

The plugin supports signing all of your release artifacts with GPG. This is a requirement when publishing to
Maven Central - our default behavior. Any version ending in `-SNAPSHOT` will never be signed. Signing parameters
can be configured via:

```properties
signing.keyId=12345678
signing.password=some_password
signing.secretKeyRingFile=/Users/yourusername/.gnupg/secring.gpg
```

It's best to place them inside your home directory, `$HOME/.gradle/gradle.properties`. You can find more information
about these properties in [Gradle's documentaion](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials).

In case you want to use in memory signing keys, which works great for CI, you can specify them like this instead:
```properties
signingInMemoryKey=exported_ascii_armored_key
# Optional.
signingInMemoryKeyId=24875D73
# If key was created with a password.
signingInMemoryPassword=secret
```

These properties can also be provided as environment variables by prefixing them with `ORG_GRADLE_PROJECT_`

It is possible to disable signing of release artifacts directly in your build scripts (takes precedence):

```groovy
mavenPublish {
  releaseSigningEnabled = false
}
```

Alternatively, you can use a Gradle property which is recommended if you only want to sign certain builds
or only build on certain machines.

```groovy
RELEASE_SIGNING_ENABLED=false
```

### Android Variants

By default, the "release" variant will be used for publishing. Optionally, a specific variant can be defined by the plugin extension:

```groovy
mavenPublish {
  androidVariantToPublish = "demoDebug"  // or use project.property('PUBLISH_VARIANT'), etc.
}
```

### Releasing

Once `publish` is called, and if you're using a Nexus repository, you'll have to make a release. This can
be done manually by following the [release steps at sonatype](https://central.sonatype.org/pages/releasing-the-deployment.html).

Additionally, the plugin will create a `closeAndReleaseRepository` task that you can call after `publish`:

```shell
# prepare your release by assigning a version (remove the -SNAPSHOT suffix)
./gradlew publish --no-daemon --no-parallel
./gradlew closeAndReleaseRepository
```

It assumes there's only one staging repository active when closeAndReleaseRepository is called. If you have stale staging repositories, you'll have to delete them by logging at https://oss.sonatype.org (or you Nexus instance).

# Base plugin

Starting with version 0.15.0 there is a base plugin. This new plugin has the same capabilities as the main
plugin but does not configure anything automatically. In the current stage the APIs are still marked with `@Incubating`
so they might change.

In your root `build.gradle` file you can do the general configuration for all modules in your project.

```groovy
import com.vanniktech.maven.publish.SonatypeHost

allprojects {
    plugins.withId("com.vanniktech.maven.publish.base") {
        GROUP = "com.example.project"
        VERSION = "1.0.3-SNAPSHOT"

        mavenPublishing {
            publishToMavenCentral("DEFAULT")

            // Will only apply to non snapshot builds.
            // Uses credentials as described above, supports both regular and in memory signing.
            signAllPublications()

            pom {
                name = "My Library"
                description = "A description of what my library does."
                inceptionYear = "2020"
                url = "https ://github.com/username/mylibrary/"
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
    }
}
```
The above also works to configure the POM of the regular plugin without properties. It's also possible to use it in
a project where some modules use the regular and some use the base plugin.

In the individual projects you can then configure publishing like this:

```groovy
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

apply plugin: "com.vanniktech.maven.publish.base"


mavenPublishing {
    // available options:
    //   - JavaLibrary
    //   - GradlePlugin
    //   - AndroidLibrary
    //   - KotlinJvm
    //   - KotlinJs
    //   - KotlinMultiplatform
    // the first parameter configures the javadoc jar, available options:
    //   - None
    //   - Empty
    //   - Javadoc
    //   - Dokka("dokkaHtml") - the parameter is the name of the Dokka task
    // second one is for whether to publish sources, optional, defaults to true (not supported for KotlinMultiplatform(
    // AndroidLibrary has a third parameter for which variant to publish, defaults to "release"
    configure(new JavaLibrary(new JavadocJar.Javadoc(), true))
}
```

# License

Copyright (C) 2018 Vanniktech - Niklas Baudy

Licensed under the Apache License, Version 2.0
