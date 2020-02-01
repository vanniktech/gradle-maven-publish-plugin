# gradle-maven-publish-plugin

Gradle plugin that creates an `uploadArchives` task to automatically upload all of your Java, Kotlin or Android 
libraries to any Maven instance. This plugin is based on [Chris Banes initial implementation](https://github.com/chrisbanes/gradle-mvn-push) 
and has been enhanced to add Kotlin support and keep up with the latest changes.

# Set up

**module/build.gradle**

```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'com.vanniktech:gradle-maven-publish-plugin:0.8.0'
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
```
GROUP=com.test.mylibrary
POM_ARTIFACT_ID=mylibrary-runtime
VERSION_NAME=3.0.5
```

In addition, there are some optional properties to give more details about your project:

```
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

Without any further configuration the plugin has two tasks. `uploadArchives` which will upload
to Sonatype OSS (Maven Central) by default. To publish to the local maven repository on your 
machine (`~/m2/repository`) there is `installArchives`. 

The username and password for Sonatype OSS can be provided as Gradle properties or environment
variables called `SONATYPE_NEXUS_USERNAME` and `SONATYPE_NEXUS_PASSWORD` to avoid having to 
commit them.

It's also possible to modify the two existing tasks or add additional targets in your build files:

```groovy
mavenPublish {
  targets {
    // Modify the existing uploadArchives task
    uploadArchives {
      releaseRepositoryUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
      snapshotRepositoryUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
      repositoryUsername = null // This defaults to either the SONATYPE_NEXUS_USERNAME Gradle property or the system environment variable.
      repositoryPassword = null // This defaults to either the SONATYPE_NEXUS_PASSWORD Gradle property or the system environment variable.
    }
    
    // Modify the existing installArchives task
    installArchives {
       // Configure as above.
    }

    // Add a new target, in this case it will create a uploadArchivesInternalRepo task
    internalRepo {
       // Configure as above.
    }
  }
}
```

More information can be found in the [Documentation of the Extension](src/main/kotlin/com/vanniktech/maven/publish/MavenPublishPluginExtension.kt)

__Note:__ To prevent looping behavior, especially in Kotlin projects / modules, you need to run the `uploadArchives` task with `--no-daemon`and `--no-parallel` flags:

`./gradlew uploadArchives --no-daemon --no-parallel`

### Signing

The plugin supports signing all of your release artifacts with GPG. This is a requirement when publishing to 
Maven Central - our default behavior. Any version ending in `-SNAPSHOT` will never be signed. Signing parameters 
can be configured via:

```groovy
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

Alternatively you can use a Gradle property which is recommended if you only want to sign certain builds
or only build on certain machines.

```groovy
RELEASE_SIGNING_ENABLED=false
```

### Releasing

Once `uploadArchives` is called, and if you're using a Nexus repository, you'll have to make a release. This can 
be done manually by following the [release steps at sonatype](https://central.sonatype.org/pages/releasing-the-deployment.html).

Alternatively, you can configure the plugin to do so automatically:

```groovy
mavenPublish {
    // ...
    nexus {
        baseUrl = "https://your_nexus_instance" // defaults to "https://oss.sonatype.org/service/local/"
        groupId = "net.example" // defaults to the GROUP Gradle Property if not set
        respositoryUserName = "username" // defaults to the SONATYPE_NEXUS_USERNAME Gradle Property or the system environment variable
        respositoryPassword = "password" // defaults to the SONATYPE_NEXUS_PASSWORD Gradle Property or the system environment variable
    }
}
```

This will create a `closeAndReleaseRepository` task that you can call after `uploadArchives`:

```
# prepare your release by assigning a version (remove the -SNAPSHOT suffix)
./gradlew uploadArchives --no-daemon --no-parallel
./gradlew closeAndReleaseRepository
```

It assumes there's only one staging repository active when closeAndReleaseRepository is called. If you have stale staging repositories, you'll have to delete them by logging at https://oss.sonatype.org (or you Nexus instance).

# License

Copyright (C) 2018 Vanniktech - Niklas Baudy

Licensed under the Apache License, Version 2.0
