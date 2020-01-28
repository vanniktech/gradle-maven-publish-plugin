# gradle-maven-publish-plugin

Gradle plugin that creates an `uploadArchives` task to automatically upload all of your Java, Kotlin or Android libraries to any Maven instance. This plugin is based on [Chris Banes initial implementation](https://github.com/chrisbanes/gradle-mvn-push) and has been enhanced to add Kotlin support and keep up with the latest changes.

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

### Snapshots

Can be found [here](https://oss.sonatype.org/#nexus-search;quick~gradle-maven-publish-plugin). Current one is:

```groovy
classpath 'com.vanniktech:gradle-maven-publish-plugin:0.9.0-SNAPSHOT'
```

## Configuration

***Uploading:***

Those are all the available configurations - shown with default values and their types. More information can be found in the [Documentation of the Extension](src/main/kotlin/com/vanniktech/maven/publish/MavenPublishPluginExtension.kt).

```groovy
mavenPublish {
  targets {
    uploadArchives {
      releaseRepositoryUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
      snapshotRepositoryUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
      repositoryUsername = null // This defaults to either the SONATYPE_NEXUS_USERNAME Gradle property or the system environment variable.
      repositoryPassword = null // This defaults to either the SONATYPE_NEXUS_PASSWORD Gradle property or the system environment variable.
      signing = true // This defaults to true. GPG signing is required by mavenCentral. If you are deploying elsewhere, you can set this to false.
    }
  }
}
```

Once you've configured this and defined the typical pom attributes via Gradle properties you can upload your library using the `uploadArchives` task.

If you need to upload to multiple repositories you can also add additional targets.

```groovy
mavenPublish {
  targets {
    uploadArchives {
       // Configure as above.
    }

    internalRepo {
       // Configure as above.
    }

    betaRepo {
       // Configure as above.
    }
  }
}
```

This will create `uploadArchivesInternalRepo` and `uploadArchivesBetaRepo` tasks.

__Note:__ To prevent looping behavior, especially in Kotlin projects / modules, you need to run the `uploadArchives` task with `--no-daemon`and `--no-parallel` flags:

`./gradlew uploadArchives --no-daemon --no-parallel`

***Releasing:***

Once `uploadArchives` is called, and if you're using a Nexus repository, you'll have to make a release. This can be done manually by following the [release steps at sonatype](https://central.sonatype.org/pages/releasing-the-deployment.html).

Alternatively, you can configure the plugin to do so automatically:

```groovy
mavenPublish {
    // ...
    nexus {
        baseUrl = "https://your_nexus_instance" // defaults to "https://oss.sonatype.org/service/local/"
        groupId = "net.example" // defaults to the GROUP Gradle Property if not set
        respositoryUserName = "username" // defaults to the SONATYPE_NEXUS_USERNAME Gradle Property if not set
        respositoryPassword = "password" // defaults to the SONATYPE_NEXUS_PASSWORD Gradle Property if not set
    }
}
```

This will create a `closeAndReleaseRepository` task that you can call after `uploadArchives`:

```
# prepare your release by assigning a version (remove the -SNAPSHOT suffix)
./gradlew uploadArchives
./gradlew closeAndReleaseRepository
```

It assumes there's only one staging repository active when closeAndReleaseRepository is called. If you have stale staging repositories, you'll have to delete them by logging at https://oss.sonatype.org (or you Nexus instance).

# Sample

This Gradle plugin is using itself to publish any of the updates. It applies a previously released version in the build.gradle just as mentioned above and sets the Gradle properties in this [gradle.properties](gradle.properties).

If you require your binary to be signed with GPG (mavenCentral requires this for instance), please add the following Gradle properties. It's best to place them inside your home directory, `$HOME/.gradle/gradle.properties`.
```groovy
signing.keyId=12345678
signing.password=some_password
signing.secretKeyRingFile=/Users/yourusername/.gnupg/secring.gpg
```

# License

Copyright (C) 2018 Vanniktech - Niklas Baudy

Licensed under the Apache License, Version 2.0
