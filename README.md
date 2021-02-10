# gradle-maven-publish-plugin

Gradle plugin that creates an `publish` task to automatically upload all of your Java, Kotlin or Android
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
    classpath 'com.vanniktech:gradle-maven-publish-plugin:0.13.0'
    // For Kotlin projects, you need to add Dokka.
    classpath 'org.jetbrains.dokka:dokka-gradle-plugin:1.x.x'
  }
}

apply plugin: "com.vanniktech.maven.publish"
```

Information: [This plugin is also available on Gradle plugins](https://plugins.gradle.org/plugin/com.vanniktech.maven.publish)

Snapshots can be found [here](https://oss.sonatype.org/#nexus-search;quick~gradle-maven-publish-plugin).

### Setting properties

To configure the coordinates of your published artifacts as well as the POM this plugin
uses Gradle properties. It's generally recommended to set them in your `gradle.properties`
file.

There are three required properties:
```properties
GROUP=com.test.mylibrary
POM_ARTIFACT_ID=mylibrary-runtime
VERSION_NAME=3.0.5
```

In addition, there are some optional properties to give more details about your project:

```properties
POM_NAME=My Library
POM_DESCRIPTION=A description of what my library does.
POM_INCEPTION_YEAR=2020

POM_URL=https://github.com/username/mylibrary/
POM_SCM_URL=https://github.com/username/mylibrary/
POM_SCM_CONNECTION=scm:git:git://github.com/username/mylibrary.git
POM_SCM_DEV_CONNECTION=scm:git:ssh://git@github.com/username/mylibrary.git

POM_LICENCE_NAME=The Apache Software License, Version 2.0
POM_LICENCE_URL=https://www.apache.org/licenses/LICENSE-2.0.txt
POM_LICENCE_DIST=repo

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

The username and password for Sonatype OSS can be provided as Gradle properties or environment
variables called `mavenCentralRepositoryUsername` and `mavenCentralRepositoryPassword` to avoid having to
commit them.

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

### Releasing

Once `publish` is called, and if you're using a Nexus repository, you'll have to make a release. This can
be done manually by following the [release steps at sonatype](https://central.sonatype.org/pages/releasing-the-deployment.html).

Alternatively, you can configure the plugin to do so automatically:

```groovy
mavenPublish {
    // ...
    nexus {
        baseUrl = "https://your_nexus_instance" // defaults to "https://oss.sonatype.org/service/local/"
        stagingProfile = "net.example" // defaults to the SONATYPE_STAGING_PROFILE Gradle property or the GROUP Gradle Property if not set
        respositoryUserName = "username" // defaults to the mavenCentralRepositoryUsername Gradle Property
        respositoryPassword = "password" // defaults to the mavenCentralRepositoryPassword Gradle Property
    }
}
```
The `stagingProfile` set here is either the same as your group id or a simpler version of it. When you are publishing a
library with `com.example.mylibrary` as group then it would either be the same or just `com.example`. You can find it
by looking at your [Sonatype staging profiles](https://oss.sonatype.org/#stagingProfiles) in the name and repo target
columns.

This will create a `closeAndReleaseRepository` task that you can call after `publish`:

```shell
# prepare your release by assigning a version (remove the -SNAPSHOT suffix)
./gradlew publish --no-daemon --no-parallel
./gradlew closeAndReleaseRepository
```

It assumes there's only one staging repository active when closeAndReleaseRepository is called. If you have stale staging repositories, you'll have to delete them by logging at https://oss.sonatype.org (or you Nexus instance).

# License

Copyright (C) 2018 Vanniktech - Niklas Baudy

Licensed under the Apache License, Version 2.0
