# Modern Gradle Publish Plugin


## Goals

- less magic configuration, more explicit APIs
- provide APIs for everything represented through properties
- remove or deprecate APIs that are already provided by Gradle
- continue providing a way to get started with minimal configuration

## New Structure

For compatibility purposes the current `com.vanniktech.maven.publish` plugin
behavior is kept as it is, with the exception of APIs that we decide to remove. 
Most of the goals are solved through a set of new plugins for specific 
platforms. These plugins provide APIs that replace the magic setup mechanisms
of the current plugin. To avoid having to maintain two implentations the old
plugin will internally just apply the new plugins and use their public APIs. 
Over time we can decide if `com.vanniktech.maven.publish` should be deprecated
or removed.

### Plugins

Specific plugins for each supported project type:
- `com.vanniktech.maven.publish.java`
- `com.vanniktech.maven.publish.gradle`
- `com.vanniktech.maven.publish.android`
- `com.vanniktech.maven.publish.kotlin.jvm`
- `com.vanniktech.maven.publish.kotlin.js`
- `com.vanniktech.maven.publish.kotlin.mpp`

The main motivation for separating these, is to provide specific APIs for each project
without running into race conditions when something like `plugins.withId` is used or 
requiring ordering. Another reason is that it allows us to nicely separate the code. 

On top of that there will be a `com.vanniktech.maven.publish.base` plugin that 
gets applied by all of these and the existing `com.vanniktech.maven.publish` as 
described above.

#### `com.vanniktech.maven.publish.base`

- applies `maven-publish` Gradle plugin
- provides the API that is shared for all project types
- can be used for shared configurations using `plugins.withId(...)` in projects with mixed types

```
mavenPublish {
  # applies signing plugin and tells Gradle to sign all publications
  signAllPublications()
  # adds the Sonatype release/snapshot repo as targets based on version
  # uses Gradle credentials API for username/password
  # automatically calls signAllPublications()
  # sets nexus.baseUrl, nexus.respositoryUserName, nexus.respositoryPassword
  publishToMavenCentral()

  # Same API as Gradle's, can probably just use their interfaces/classes
  # Will be applied to all publications
  pom {
    name = "My Library"
    description = "A description of what my library does."
    inceptionYear = "2020"
    url = "https://github.com/username/mylibrary/"
    
    scm {
        connection = "scm:git:git://github.com/username/mylibrary.git"
        developerConnection = "scm:git:ssh://git@github.com/username/mylibrary.git"
        url = "https://github.com/username/mylibrary/"
    } 
    
    licenses {
        license {
            name = "The Apache Software License, Version 2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            distribution = "repo"
        }
    }
    developers {
        developer {
            id = "username"
            name = "User Name"
            url = "https://github.com/username/"
        }
    }
  }

  # Configure closeAndReleaseRepository
  nexus {
     baseUrl = "https://your_nexus_instance"
     stagingProfile = "net.example"
     # Start supporting the Gradle credentials API
     respositoryUserName = "username"
     respositoryPassword = "password"
  }
}
```

#### `com.vanniktech.maven.publish.java` & `com.vanniktech.maven.publish.kotlin.jvm`

Meant to be used with `java`, `java-library`, `org.jetbrains.kotlin.jvm`. 

Pure Gradle configuration:
```
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

java {
  withSourcesJar()
  withJavadocJar()
}
```

The plugin doesn't need to offer anything for sources and javadocs jars. It will
simply create a publication using `components["java"]`.

#### `com.vanniktech.maven.publish.gradle`

Meant to be used with `java-gradle-plugin`.

Pure Gradle configuration:
```
java {
  withSourcesJar()
  withJavadocJar()
}
```

Gradle automatically adds a publication when both `java-gradle-plugin` and 
`maven-publish` are applied. There is no need for our own APIs, so this plugin
does not have any functionality of it's own.

#### `com.vanniktech.maven.publish.android`

Meant to be used with `com.android.library`.

Pure Gradle configuration:
```
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["release"])
            }
        }
    }
}
```


There is no API available to create source and javadoc jars for Android 
projects. The plugin will provide an API to configure those as well as 
which variant to publish. These properties/methods can be removed/deprecated once
AGP offers it's own methods and adds multi variant publishing.

```
androidPublishCompat {
  androidVariantToPublish = "release"
  withSourcesJar()
  withJavadocJar()
}
```

#### `com.vanniktech.maven.publish.kotlin.js`

Meant to be used with `org.jetbrais.kotlin.js`.

Pure Gradle configuration:
```publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
            artifact(project.tasks.named("kotlinSourcesJar"))
        }
    }
}
```


There is no API available to create source and javadoc jars for Kotlin JS
projects, but there is at least a task for sources. The plugin will provide 
an API to configure those. These properties/methods can be removed/deprecated once
Kotlin offers it's own methods and adds multi variant publishing.

```
androidPublishCompat {
  androidVariantToPublish = "release"
  withSourcesJar()
  withJavadocJar()
}
```


#### `com.vanniktech.maven.publish.kotlin.mpp`

Meant to be used with `org.jetbrais.kotlin.multiplatform`.

Pure Gradle configuration: n/a

Like `java-gradle-plugin` the Kotlin MPP plugin sets up a publication 
automatically. It also adds source jars automatically. This plugin adds
an API to add javadoc publishing. There still is a `withSourcesJar()` which will
add empty source jars to all publications missing one, this can be removed once
the behavior is fixed on the Kotlin side.

```
kotlinMultiplatformPublishCompat {
  withSourcesJar()
  withJavadocJar()
}
```

#### `com.vanniktech.maven.publish`

- calls `publishToMavenCentral()`
- sets `groupId`, `version` and `baseArtifactId` based on Gradle properties
- configures `pom` based on Gradle properties (warn about deprecation)
- applies project type plugin automatically based on other applied plugins
- calls all API methods of the project type plugin
