# gradle-maven-publish-plugin

Gradle plugin that creates a `publish` task to automatically upload all of your Java, Kotlin or Android
libraries to any Maven instance. This plugin is based on [Chris Banes initial implementation](https://github.com/chrisbanes/gradle-mvn-push)
and has been enhanced to add Kotlin support and keep up with the latest changes.

# Setup

- [Publishing open source projects to Maven Central](https://vanniktech.github.io/gradle-maven-publish-plugin/central/)
- [Publishing to other Maven repositories](https://vanniktech.github.io/gradle-maven-publish-plugin/other/)

For modifying what is getting published see [configuring what to publish](https://vanniktech.github.io/gradle-maven-publish-plugin/what/).

There is also a [base plugin](https://vanniktech.github.io/gradle-maven-publish-plugin/base/) that doesn't apply any
default configuration and allows the most customization.

# Supported plugins

The output of the following Gradle plugins is supported to be published with this plugin:

- `com.android.fused-library`
- `com.android.kotlin.multiplatform.library`
- `com.android.library`
- `com.gradle.plugin-publish`
- `java`
- `java-gradle-plugin`
- `java-library`
- `java-platform`
- `org.jetbrains.kotlin.jvm`
- `org.jetbrains.kotlin.multiplatform`
- `version-catalog`

# Advantages over `maven-publish`

Gradle ships with the `maven-publish` and many other plugins like the Android Gradle Plugin or the Kotlin Multiplatform
plugin directly integrate with, so why should you use this plugin?

- **No need to know how publishing works for different project types**. AGP provides an API to configure publishing,
  `java-library` too, Kotlin Multiplatform does most things automatically but not everything. This plugin configures
  as much as possible on its  own.
- **An unified approach for all kinds of projects**. Some parts require manual configuration and for those we provide an API
  that works regardless of whether this is a Gradle plugin, an Android library or a Kotlin Multiplatform project. This
  is especially useful for projects that combine multiple of these.
- **Maven central integration**. The plugin makes it easy to configure publishing to Maven Central with dedicated
  APIs to set it up and configure everything that is required. It also supports automatic releasing without requiring
  any interaction with the web interface.
- **In memory GPG signing keys**. Easily sign artifacts on CI by simply setting the required environment variables,
  no extra setup required.
- **Gradle property based config**. Easily configure the plugin with Gradle properties that will apply to all
  subprojects

# License

Copyright (C) 2018 Vanniktech - Niklas Baudy

Licensed under the Apache License, Version 2.0
