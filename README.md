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
    classpath 'com.vanniktech:gradle-maven-publish-plugin:0.2.0'
  }
}

apply plugin: "com.vanniktech.maven.publish"
```

Information: [This plugin is also available on Gradle plugins](https://plugins.gradle.org/plugin/com.vanniktech.maven.publish)

### Snapshots

Can be found [here](https://oss.sonatype.org/#nexus-search;quick~gradle-maven-publish-plugin). Current one is:

```groovy
classpath 'com.vanniktech:gradle-maven-publish-plugin:0.2.0-SNAPSHOT'
```

## Configuration

Those are all the available configurations - shown with default values and their types. More information can be found in the [Documentation of the Extension](src/test/kotlin/com/vanniktech/maven/publish/MavenPublishPluginExtensionTest.kt).

```groovy
mavenPublish {
  String releaseRepositoryUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
  String snapshotRepositoryUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
  String repositoryUsername = null // This defaults to either the SONATYPE_NEXUS_USERNAME Gradle property or the system environment variable.
  String repositoryPassword = null // This defaults to either the SONATYPE_NEXUS_PASSWORD Gradle property or the system environment variable.
}
```

Once you've configured this you and defined the typical pom attributes via Gradle properties you can upload your library using the `uploadArchives` task.

# Sample

This Gradle plugin is using itself to publish any of the updates. It applies a previously released version in the build.gradle just as mentioned above and sets the Grale properties in this [gradle.properties](gradle.properties).

# License

Copyright (C) 2018 Vanniktech - Niklas Baudy

Licensed under the Apache License, Version 2.0
