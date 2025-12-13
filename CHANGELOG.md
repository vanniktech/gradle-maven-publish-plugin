# Change Log

## [UNRELEASED](https://github.com/vanniktech/gradle-maven-publish-plugin/compare/0.35.0...HEAD) *(2025-xx-xx)*

- Updated minimum supported JDK, Gradle, Android Gradle Plugin and Kotlin versions.
- Removed support for Dokka v1, it's now required to use Dokka in v2 mode.
- Removed deprecated option of selecting which Android variant to publish for KMP libraries.
- `validateDeployment` now has the `DeploymentValidation` enum as type instead of being a boolean. This
  allows setting it to `VALIDATE` which only waits for validations instead of the full publishing process.
- Changed the default `SONATYPE_CLOSE_TIMEOUT_SECONDS` to 60 minutes.
- When enabling Maven Central publishing through the DSL, the `mavenCentralDeploymentValidation` and
  `mavenCentralAutomaticPublishing` are used for the default values of the 2 parameters when they are not passed
  explicitly. This allows to more easily override them in certain environments.

**BREAKING**
- Mark `DirectorySignatureType` internal.

#### Minimum supported versions
- JDK 17
- Gradle 9.0.0
- Android Gradle Plugin 8.13.0
- Kotlin Gradle Plugin 2.2.0

#### Compatibility tested up to
- JDK 25
- Gradle 9.2.0
- Gradle 9.3.0-milestone-1
- Android Gradle Plugin 8.13.1
- Android Gradle Plugin 9.0.0-alpha14
- Kotlin Gradle Plugin 2.2.21
- Kotlin Gradle Plugin 2.3.0-Beta2


## [0.35.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.35.0) *(2025-11-11)*

- Add support for publishing Kotlin Multiplatform libraries that use `com.android.kotlin.multiplatform.library`.
- Add support for validating deployments to Central Portal
- Raise minimum Gradle version to 8.13
- Raise minimum Android Gradle Plugin version to 8.2.2
- Do not unconditionally disable DocLint
- Fail publishing if `SONATYPE_HOST` is not set to `CENTRAL_PORTAL`.
- Fix misleading error message when Android library variant is not found.
- Downgrade transitive OkHttp version.
- Don't check project heirarchy for POM properties when Isolated proejcts is enabled.

Thanks to @joshfriend, @Flowdalic and @Goooler for their contributions to this release.

#### Minimum supported versions
- JDK 11
- Gradle 8.13
- Android Gradle Plugin 8.2.2
- Kotlin Gradle Plugin 1.9.20

#### Compatibility tested up to
- JDK 24
- Gradle 9.2.0
- Gradle 9.3.0-milestone-1
- Android Gradle Plugin 8.13.1
- Android Gradle Plugin 9.0.0-alpha14
- Kotlin Gradle Plugin 2.2.21
- Kotlin Gradle Plugin 2.3.0-Beta2


## [0.34.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.34.0) *(2025-07-13)*

- Added configuration cache support for publishing.
- Removed support for publishing through Sonatype OSSRH since it has been shut down. See the
  [0.33.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.33.0) release notes for upgrade
  instructions if you haven't upgraded yet.
    - `SonatypeHost` has been removed from the DSL.
    - `SONATYPE_HOST` only supports `CENTRAL_PORTAL` now. It's recommended to use the following properties instead:
        - `mavenCentralPublishing=true` replaces `SONATYPE_HOST=CENTRAL_PORTAL`.
        - `mavenCentralAutomaticPublishing=true` replaces `SONATYPE_AUTOMATIC_RELEASE=true`.
- Update the Central Portal Publisher APIs to the latest.
- It's now possible to mix SNAPSHOT versions and release versions when running `publish` tasks.
- Fixed Gradle's deprecation warning caused by invalid URI.
- Fixed check for the minimum supported Gradle version running too late in some cases.

Thanks to @Goooler and @solrudev for their contributions to this release.

#### Minimum supported versions
- JDK 11
- Gradle 8.5
- Android Gradle Plugin 8.0.0
- Kotlin Gradle Plugin 1.9.20

#### Compatibility tested up to
- JDK 24
- Gradle 8.14.3
- Gradle 9.0.0-rc2
- Android Gradle Plugin 8.11.1
- Android Gradle Plugin 8.12.0-alpha08
- Kotlin Gradle Plugin 2.2.0
- Kotlin Gradle Plugin 2.2.20-Beta1


## [0.33.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.33.0) *(2025-06-22)*

> [!CAUTION]
> Sonatype OSSRH (oss.sonatype.org and s01.oss.sonatype.org) will be shut down on June 30, 2025.
>
> Migration steps:
> 1. Sign in to the [Central Portal](https://central.sonatype.com) with your existing Sonatype account.
> 2. Go to [Namespaces](https://central.sonatype.com/publishing/namespaces) and click "Migrate Namespace"
>    for the relevant namespace. Confirm the migration and wait for it to complete.
> 3. Optional: If you want to publish snapshots of your project tap the 3 dots next to your namespace and
>    select "Enable SNAPSHOTs".
> 4. Go to [Account](https://central.sonatype.com/account) and select "Generate User Token". Use the shown
>    "Username" and "Password" as values for `mavenCentralUsername` and `mavenCentralPassword`.
> 5. Configure this plugin to publish to Central Portal. Either update your `SONATYPE_HOST` property from
>    `DEFAULT` or `S01` to `CENTRAL_PORTAL` or call `publishToMavenCentral()`/`publishToMavenCentral(automaticRelease)`
>    without a `SonatypeHost` parameter.

**BREAKING**
- `publishToMavenCentral()` and `publishToMavenCentral(automaticRelease)` without `SonatypeHost` will
  now publish through the Central Portal.
- Deprecated overloads of `publishToMavenCentral` that take a `SonatypeHost` parameter.
- Deprecated `SonatypeHost`.

New
- Basic experimental support for `com.android.fused-library`. There are currently several limitations
  on the Android Gradle plugin side which make signing as well as publishing sources/javadocs not possible.

Improvements
- Added new Gradle properties:
  - `mavenCentralPublishing=true` replaces `SONATYPE_HOST=CENTRAL_PORTAL`.
  - `mavenCentralAutomaticPublishing=true` replaces `SONATYPE_AUTOMATIC_RELEASE=true`.
  - `signAllPublications=true` replaces `RELEASE_SIGNING_ENABLED=true`.
  - Note: The old properties continue to work and there are no plans to remove them.
- The base plugin is now compatible with isolated projects as long as `pomFromGradleProperties()` is not called.
- It's possible to pass a `TaskProvider` to `JavadocJar.Dokka`.
- Improved naming of produced `-javadoc` jars (locally, the name of the published artifact is unchanged).
- Resolve issue that caused `version` to be read too early when publishing to Central Portal.


Thanks to @Goooler, @solrudev and @sschuberth for their contributions to this release.

#### Minimum supported versions
- JDK 11
- Gradle 8.5
- Android Gradle Plugin 8.0.0
- Kotlin Gradle Plugin 1.9.20

#### Compatibility tested up to
- JDK 24
- Gradle 8.14.2
- Gradle 9.0-rc1
- Android Gradle Plugin 8.10.0
- Android Gradle Plugin 8.11.0-rc02
- Android Gradle Plugin 8.12.0-alpha06
- Kotlin Gradle Plugin 2.1.21
- Kotlin Gradle Plugin 2.2.0-RC3


## [0.32.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.32.0) *(2025-05-15)*

- Improve names of Central Portal deployments.
- Fix an issue where the wrong staging profile for (s01.)oss.sonatype.org was selected when publishing to Maven Central.
- Fix incompatibility with Kotlin 1.9.x when used through compiled plugins.
- Improve error message when not being able to detect Kotlin plugin.
- Fix an issue with detecting whether configuration cache is enabled which lead to a not actionable error message.
- Fix compatibility with Gradle 9.0.

#### Minimum supported versions
- JDK 11
- Gradle 8.5
- Android Gradle Plugin 8.0.0
- Kotlin Gradle Plugin 1.9.20

#### Compatibility tested up to
- JDK 24
- Gradle 8.14
- Gradle 9.0-milestone-6
- Android Gradle Plugin 8.10.0
- Android Gradle Plugin 8.11.0-alpha10
- Kotlin Gradle Plugin 2.1.20
- Kotlin Gradle Plugin 2.1.21-RC2
- Kotlin Gradle Plugin 2.2.0-Beta2

#### Configuration cache status

Configuration cache is generally supported, except for:
- Publishing releases to Maven Central (snapshots are fine), blocked by [Gradle issue #22779](https://github.com/gradle/gradle/issues/22779).
- When using Dokka 1.x or Dokka 2.x without `V2Enabled`.


## [0.31.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.31.0) *(2025-03-06)*

- Add support for publishing snapshots to Central Portal.
  - Make sure to enable snapshots for your namespace on central.sonatype.com.
  - Thanks to @solrudev for the contribution.
- Add support for multiple matching staging profiles by taking the one with the longest matching prefix.

#### Minimum supported versions
- JDK 11
- Gradle 8.5
- Android Gradle Plugin 8.0.0
- Kotlin Gradle Plugin 1.9.20

#### Compatibility tested up to
- JDK 23
- Gradle 8.13
- Android Gradle Plugin 8.9.0
- Android Gradle Plugin 8.10.0-alpha07
- Kotlin Gradle Plugin 2.1.10
- Kotlin Gradle Plugin 2.1.20-RC

#### Configuration cache status

Configuration cache is generally supported, except for:
- Publishing releases to Maven Central (snapshots are fine), blocked by [Gradle issue #22779](https://github.com/gradle/gradle/issues/22779).
- When using Dokka 1.x or Dokka 2.x without `V2Enabled`.


## [0.30.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.30.0) *(2024-10-13)*

- Add support for Dokka 2.0.0-Beta
  - Supports `org.jetbrains.dokka.experimental.gradle.pluginMode=V2Enabled`.
  - Supports both `org.jetbrains.dokka` and `org.jetbrains.dokka-javadoc`.
  - If both are applied the javadoc output is published.
  - Removed support for the old `org.jetbrains.dokka-android` plugin.
- Support custom Sonatype hosts by providing a `https` url in `SONATYPE_HOST` Gradle property.
- Remove usages of deprecated Gradle API that is scheduled to be removed in Gradle 9.0
- Raised minimum supported Gradle version.
- Improve naming of javadoc jars.

#### Minimum supported versions
- JDK 11
- Gradle 8.5
- Android Gradle Plugin 8.0.0
- Kotlin Gradle Plugin 1.9.20

#### Compatibility tested up to
- JDK 23
- Gradle 8.10.2
- Android Gradle Plugin 8.7.0
- Android Gradle Plugin 8.8.0-alpha05
- Kotlin Gradle Plugin 2.0.20
- Kotlin Gradle Plugin 2.1.0-Beta1

#### Configuration cache status

Configuration cache is generally supported, except for:
- Publishing releases to Maven Central (snapshots are fine), blocked by [Gradle issue #22779](https://github.com/gradle/gradle/issues/22779).
- When using Dokka 1.x or Dokka 2.x without `V2Enabled`.


## [0.29.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.29.0) *(2024-06-20)*

- Added `configureBasedOnAppliedPlugins(sourcesJar: Boolean, javadocJar: Boolean)`
  overload that allows disabling sources and javadoc jars without having to use the more granular `Platform` APIs.
- For Java library and Kotlin/JVM projects the Gradle module metadata now properly includes the sources jar.
- When running on Gradle 8.8 or newer the pom configuration is not applied in
  `afterEvaluate` anymore, making manual overrides easier.
- Fix potential issue with the javadoc jar tasks that can cause Gradle to disable optimizations.
- When staging profiles can't be loaded the status code of the response is added to the error message.

#### Minimum supported versions
- JDK 11
- Gradle 8.1
- Android Gradle Plugin 8.0.0
- Kotlin Gradle Plugin 1.9.20

#### Compatibility tested up to
- JDK 21
- Gradle 8.8
- Android Gradle Plugin 8.5.0
- Android Gradle Plugin 8.6.0-alpha06
- Kotlin Gradle Plugin 2.0.0
- Kotlin Gradle Plugin 2.0.20-Beta1

#### Configuration cache status

Configuration cache is generally supported, except for:
- Publishing releases to Maven Central (snapshots are fine), blocked by [Gradle issue #22779](https://github.com/gradle/gradle/issues/22779).
- Dokka does not support configuration cache.


## [0.28.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.28.0) *(2024-03-12)*

- Added support for publishing through the new [Central Portal](https://central.sonatype.com). To use
  this use the `CENTRAL_PORTAL` option when specifying the Sonatype host.
- For Kotlin Multiplatform the main plugin will now automatically publish the `release` variant if the project
  has an Android target and no variant was explicitly specified through the Kotlin Gradle DSL.
- Support specifying the Android variants to publish in `KotlinMultiplatform(...)`.
- Updated minimum supported Gradle, Android Gradle Plugin and Kotlin versions.
- Removed support for the deprecated Kotlin/JS plugin.
- Removed the deprecated `closeAndReleaseRepository` task. Use `releaseRepository`, which
  is functionally equivalent, instead.

#### Minimum supported versions
- JDK 11
- Gradle 8.1
- Android Gradle Plugin 8.0.0
- Kotlin Gradle Plugin 1.9.20

#### Compatibility tested up to
- JDK 21
- Gradle 8.6
- Gradle 8.7-rc-3
- Android Gradle Plugin 8.3.0
- Android Gradle Plugin 8.4.0-alpha13
- Kotlin Gradle Plugin 1.9.23
- Kotlin Gradle Plugin 2.0.0-Beta4

#### Configuration cache status

Configuration cache is generally supported, except for:
- Publishing releases to Maven Central (snapshots are fine), blocked by [Gradle issue #22779](https://github.com/gradle/gradle/issues/22779).
- Dokka does not support configuration cache.


## [0.27.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.27.0) *(2024-01-06)*

- Added new publishing related tasks
  - `releaseRepository` releases a staging repository at the end of the build and can be executed in the same build
    as the publishing task. This allows having automatic releases without permanently enabling them.
  - `publishToMavenCentral` as alias for running `publishAllPublicationsToMavenCentralRepository`.
  - `publishAndReleaseToMavenCentral` as alias for running both of the above.
  - For more information [checkout the docs](https://vanniktech.github.io/gradle-maven-publish-plugin/central/#publishing-releases).
- It is now possible to only pass a subset of the parameters to
  `coordinates(...)` and leave the others at their default value.
  Thanks to @sschuberth for the contribution.
- Fixed `java-test-fixture` projects being broken with Gradle 8.6.
- Deprecated `closeAndReleaseRepository` in favor of `releaseRepository`.

#### Minimum supported versions
- JDK 11
- Gradle 7.6
- Android Gradle Plugin 7.4.0
- Kotlin Gradle Plugin 1.8.20

#### Compatibility tested up to
- JDK 21
- Gradle 8.5
- Gradle 8.6-rc-1
- Android Gradle Plugin 8.2.1
- Android Gradle Plugin 8.3.0-beta01
- Android Gradle Plugin 8.4.0-alpha03
- Kotlin Gradle Plugin 1.9.22
- Kotlin Gradle Plugin 2.0.0-Beta2

#### Configuration cache status

When using **Gradle 8.1** or newer configuration cache is generally supported.

Exceptions to that are:
- Publishing releases to Maven Central (snapshots are fine), blocked by [Gradle issue #22779](https://github.com/gradle/gradle/issues/22779).
- Dokka does not support configuration cache.


## [0.26.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.26.0) *(2023-12-19)*

- It's now supported to call `configure(Platform)` from the main plugin to modify
  what is getting published. [Check out the docs for more details](https://vanniktech.github.io/gradle-maven-publish-plugin/what/)
- The base plugin now has a `configureBasedOnAppliedPlugins` DSL method to
  allow applying the default `configure` logic of the main plugin.
- Calling `configure(Platform)` now validates that the required plugins are
  applied (e.g. Android Gradle Plugin for Android projects).
- It's now possible to disable source publishing for KMP projects.
- Fixed an issue which would cause the javadoc jar task to be registered multiple
  times for Gradle plugin projects with more than one publication. Thanks to
  @autonomousapps for the fix.
- Publishing Kotlin/JS projects has been deprecated and will be removed in the
  next release, because the Kotlin/JS plugin has been deprecated.
- The internal task to create a javadoc jar for certain project types has been renamed
  from `simpleJavadocJar` to `plainJavadocJar`. Thanks to @sschuberth.

#### Minimum supported versions
- JDK 11
- Gradle 7.6
- Android Gradle Plugin 7.4.0
- Kotlin Gradle Plugin 1.8.20

#### Compatibility tested up to
- JDK 21
- Gradle 8.5
- Android Gradle Plugin 8.2.0
- Android Gradle Plugin 8.3.0-alpha17
- Kotlin Gradle Plugin 1.9.21
- Kotlin Gradle Plugin 2.0.0-Beta1

#### Configuration cache status

When using **Gradle 8.1** or newer configuration cache is generally supported.

Exceptions to that are:
- Publishing releases to Maven Central (snapshots are fine), blocked by [Gradle issue #22779](https://github.com/gradle/gradle/issues/22779).
- Dokka does not support configuration cache.


## [0.25.3](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.25.3) *(2023-07-01)*

- Gradle 8.2: Fix error for projects that use the `java-test-fixtures` plugin.
- Fix issue for Kotlin Multiplatform projects when running tests and having signing enabled.


## [0.25.2](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.25.2) *(2023-04-16)*

- Fix javadoc jar being empty when using dokka.


## [0.25.1](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.25.1) *(2023-03-24)*

- Fix snapshot publishing being broken.


## [0.25.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.25.0) *(2023-03-23)*

- The `createStagingRepository` task now uses the worker API which allows the project to built-in
  parallel to the creation of the staging repository.
- Fix incompatibility with Kotlin 1.8.20-Beta for Kotlin/JS projects. The Kotlin/JS plugin is now taking
  care of creating the sources jar on its own. Because of this the base plugin won't allow disabling
  sources jar creation for Kotlin/JS projects anymore starting with 1.8.20. The `KotlinJs` constructor
  with a `sourcesJar` parameter has been deprecated.
- Fix incompatibility with Gradle 8.1 for `java-test-fixtures` projects.
- Fix incompatibility with `com.gradle.plugin-publish` 1.0.0 and 1.1.0.
- New minimum supported versions:
  - Gradle 7.4
  - Android Gradle Plugin 7.3.0
  - Kotlin Gradle Plugin 1.7.0
  - `com.gradle.plugin-publish` 1.0.0
- Note: Starting with Kotlin 1.8.20-Beta the `common` sources jar for multiplatform projects will only contain
  the sources of the common source set instead of containing the sources from all source sets.

#### Configuration cache status

Configuration cache is supported starting with **Gradle 7.6+** except for:
- Builds with enabled signing, will be resolved in Gradle 8.1.
- Publishing releases to Maven Central (snapshots are fine), blocked by [Gradle issue #22779](https://github.com/gradle/gradle/issues/22779).
- Kotlin Multiplatform projects, blocked by [KT-49933](https://youtrack.jetbrains.com/issue/KT-49933).


## [0.24.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.24.0) *(2023-01-29)*

- Support arbitrary Sonatype hosts instead of just oss.sonatype.org and s01.oss.sonatype.org.
- Support adjusting timeouts for Sonatype related requests and operations. [See docs](https://vanniktech.github.io/gradle-maven-publish-plugin/central/#timeouts)
- Internal change on how the sources jar is created.


## [0.23.2](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.23.2) *(2023-01-17)*

- Fix signing when using Gradle 8.0.
- Finding a matching staging profile in Sonatype is more lenient. If there is just one that one will always be used.
  The plugin will also fallback to any staging profile that has a matching prefix with the group id.
- As a workaround for an issue in Gradle that causes invalid module metadata for `java-test-fixtures` projects,
  `project.group` and `project.version` are now being set again for those projects. [#490](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/490)


## [0.23.1](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.23.1) *(2022-12-30)*

- Also support publishing sources for the `java-test-fixtures` plugin in Kotlin/JVM projects.
- Suppress Gradle warnings when publishing a project that uses `java-test-fixtures`.


## [0.23.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.23.0) *(2022-12-29)*

Updated docs can be found on [the new website](https://vanniktech.github.io/gradle-maven-publish-plugin/).

- **NEW**: It is now possible to set group id, artifact id directly through the DSL
  ```groovy
  mavenPublishing {
    coordinates("com.example", "library", "1.0.3")
  }
  ```
- `project.group` and `project.version` will still be used as default values for group and version if the
  `GROUP`/`VERSION_NAME` Gradle properties do not exist and `coordinates` was not called, however there are 2
  **behavior changes**:
  - The `GROUP` and `VERSION_NAME` Gradle properties take precedence over `project.group` and `project.version` instead
    of being overwritten by them. If you need to define the properties but replace them for some projects,
    please use the new `coordinates` method instead.
  - The `GROUP` and `VERSION_NAME` Gradle properties will not be explicitly set as `project.group` and
    `project.version` anymore.
- **NEW**: Added `dropRepository` task that will drop a Sonatype staging repository. It is possible to specify
  which repository to drop by adding a `--repository` parameter with the id of the staging repository that was
  printed during `publish`. If no repository is specified and there is only one staging repository, that one
  will be dropped.
- Added workaround to also publish sources for the `java-test-fixtures` plugin.
- Fixed publishing Kotlin/JS projects with the base plugin.
- Fixed that a POM configured through the DSL is incomplete when publishing Gradle plugins.
- The minimum supported Gradle version has been increased to 7.3.
- The plugin now requires using JDK 11+ to run Gradle.


## [0.22.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.22.0) *(2022-09-09)*

- **NEW**: When publishing to maven central by setting `SONATYPE_HOST` or calling `publishToMavenCentral(...)`
  the plugin will now explicitly create a staging repository on Sonatype. This avoids issues where a single build would
  create multiple repositories.
- The above change means that the plugin supports parallel builds, and it is not necessary anymore to use
  `--no-parallel` and `--no-daemon` together with `publish`
- **NEW**: When publishing with the `publish` or `publishAllPublicationsToMavenCentralRepository` tasks
  the plugin will automatically close the staging repository at the end of the build if it was successful.
- **NEW**: Option to also automatically release the staging repository after closing was successful.
```
SONATYPE_HOST=DEFAULT # or S01
SONATYPE_AUTOMATIC_RELEASE=true
```
or
```
mavenPublishing {
  publishToMavenCentral("DEFAULT", true)
  // or publishToMavenCentral("S01", true)
}
```
- in case the option above is enabled, the `closeAndReleaseRepository` task is not needed anymore.
- when closing the repository fails the plugin will fail the build immediately instead of timing out.
- when closing the repository fails the plugin will try to print the error messages from Nexus.
- increased timeouts for calls to the Sonatype Nexus APIs.
- fixed incompatibility with the `com.gradle.plugin-publish` plugin.
- added workaround for Kotlin multiplatform builds reporting disabled build optimizations. (see [KT-46466](https://youtrack.jetbrains.com/issue/KT-46466))


## [0.21.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.21.0) *(2022-07-11)*

Minimum supported Gradle version is now 7.2.0

Minimum supported Android Gradle Plugin versions are now 7.1.2, 7.2.0-beta02 and 7.3.0-alpha01

**Behavior changes**

The `com.vanniktech.maven.publish` stops adding Maven Central (Sonatype OSS) as a
publishing target and will not enable GPG signing by default. To continue publishing to maven central and signing artifacts either add the following to your `gradle.properties`:
```properties
SONATYPE_HOST=DEFAULT
# SONATYPE_HOST=S01 for publishing through s01.oss.sonatype.org
RELEASE_SIGNING_ENABLED=true
```

or add this to your Groovy build files:
```gradle
mavenPublishing {
  publishToMavenCentral()
  // publishToMavenCentral("S01") for publishing through s01.oss.sonatype.org
  signAllPublications()
}
```
or the following to your kts build files:
```kotlin
mavenPublishing {
  publishToMavenCentral()
  // publishToMavenCentral(SonatypeHost.S01) for publishing through s01.oss.sonatype.org
  signAllPublications()
}
```

The base plugin is unaffected by these changes because it already has this behavior.

**Android variant publishing**

Since version 0.19.0 the plugin was publishing a multi variant library by
default for Android projects. Due to [a bug in Android Studio](https://issuetracker.google.com/issues/197636221)
that will cause it to not find the sources for libraries published this way the
plugin will temporarily revert to publishing single variant libraries again.
Unless another variant is specified by setting the `ANDROID_VARIANT_TO_PUBLISH`
Gradle property the `release` variant will be published.

To continue publishing multi variant libraries you can use the
[base plugin](https://github.com/vanniktech/gradle-maven-publish-plugin#base-plugin).

**Removals**

The deprecated `mavenPublish` extension has been removed. Take a look at the
changelog for 0.20.0 for replacements.


## [0.20.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.20.0) *(2022-06-02)*

**Upcoming behavior change**

In the next release after this the `com.vanniktech.maven.publish` will stop adding Maven Central (Sonatype OSS) as a
publishing target and will not enable GPG signing by default. If you are currently relying on this behavior the plugin
will print a warning during configuration phase. To continue publishing to maven central and signing artifacts either
add this to your build files:
```gradle
mavenPublishing {
  publishToMavenCentral() // use publishToMavenCentral("S01") for publishing through s01.oss.sonatype.org
  signAllPublications()
}
```
or the following to your `gradle.properties`:
```gradle
SONATYPE_HOST=DEFAULT
# SONATYPE_HOST=S01 for publishing through s01.oss.sonatype.org
RELEASE_SIGNING_ENABLED=true
```

The base plugin is unaffected by these changes because it already has this behavior.

**Deprecation**

The old `mavenPublish` extension has been deprecated.

If you were using it to set `sonatypeHost` to `S01` use
```gradle
mavenPublishing {
  publishToMavenCentral("S01")
}
```
instead or add `SONATYPE_HOST=S01` to your gradle.properties.

If `sonatypeHost` was used to disable adding Maven Central as a publishing target add `SONATYPE_HOST=` until 0.21.0 is out and this becomes the default behavior.

If you set `releaseSigningEnabled` to false add `RELEASE_SIGNING_ENABLED=false` to your gradle.properties until 0.21.0 is out and this becomes the default behavior.


**New**

Added support to set the following pom values through properties (thanks to @jaredsburrows for the contribution)
- `POM_ISSUE_SYSTEM` sets `issueManagement.system`
- `POM_ISSUE_URL` sets `issueManagement.url`
- `POM_DEVELOPER_EMAIL` sets `developer.email`

**Fixed**

- resolved an issue in Kotlin Multiplatform projects that apply `com.android.library` that caused no sources jars to be published
- resolved an issue in Kotlin Multiplatform projects that apply `com.android.library` using AGP versions before 7.1.2 that caused the project to be published as a pure Android library
- fixed and improved error messages for `closeAndReleaseRepository`


## [0.19.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.19.0) *(2022-02-26)*

- **Behavior Change:** When using version 7.1.0 or newer of the Android Gradle Plugin we will now publish all variants
  of a library unless `androidVariantToPublish` was set in the DSL. This means that for example both `debug` and `release`
  or all flavors.
- Deprecated `androidVariantToPublish`. In the future the main plugin will always publish all variants of an Android
  library. If you need to publish only one variant or a subset take a look at the [base plugin](README.md#base-plugin)
  APIs.
- Base plugin: Added `AndroidSingleVariantLibrary` and `AndroidMultiVariantLibrary` options that use the new AGP 7.1
  APIs under the hood.
- Base plugin: Deprecated `AndroidLibrary` option in favor of the above
- The integration with Sonatype Nexus has been extracted into its own artifact and is available as `com.vanniktech:nexus:<version>`


## [0.18.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.18.0) *(2021-09-13)*

- The minimum supported Kotlin version is now `1.4.30`.
- It's now possible to specify `SONATYPE_HOST` as a Gradle property, e.g.
  - `SONATYPE_HOST=S01` for `s01.sonatype.org`
  - `SONATYPE_HOST=` to not add any repository by default
- Fixed an issue when publishing Kotlin MPP projects with the base plugin.
- Removed checks for presence of properties that aren't used by this plugin anymore.


## [0.17.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.17.0) *(2021-07-04)*

- Removed the deprecated `uploadArchives` and `installArchives` tasks. Use `publish` and `publishToMavenLocal` instead.


## [0.16.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.16.0) *(2021-06-20)*

- Add `pomFromGradleProperties` API to base plugin. This configures the pom in the same way the regular plugin does.
- Add the ability to remove the default `mavenCentral` repository, by setting `sonatypeHost` to `null`.
- Support `POM_LICENSE_NAME`, `POM_LICENSE_URL` and `POM_LICENSE_DIST` properties in addition to `LICENCE` based properties.
- Fixes an issue in the base plugin that caused an error during configuration of Android projects.
- Fixes an issue with javadoc tasks when using Java toolchains.
- The deprecated `nexusOptions` and `nexus {}` methods were removed. `closeAndReleaseRepository` is automatically configured.


## [0.15.1](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.15.1) *(2021-05-02)*

- The `closeAndReleaseRepository` task was mistakenly expecting the wrong Gradle properties. The README and changelog also mentioned the wrong properties. The correct ones are `mavenCentralUsername` and `mavenCentralPassword` or for environment variables: `ORG_GRADLE_PROJECT_mavenCentralUsername` and `ORG_GRADLE_PROJECT_mavenCentralPassword`.
- Fix `signing` not being configurable until `afterEvaluate`.
- Use empty string as in memory signing password when no password is specified.
- Fix `statingProfile` in `nexusOptions` not being optional which causes an error when running `closeAndReleaseRepository`.


## [0.15.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.15.0) *(2021-04-24)*

- **BREAKING**: Removed support for deprecated `RELEASE_REPOSITORY_URL`, `SNAPSHOT_REPOSITORY_URL`, `SONATYPE_NEXUS_USERNAME`, `SONATYPE_NEXUS_PASSWORD` environment variables and properties.
  For safety reasons the project will fail when finding these. Use `mavenCentralUsername` and `mavenCentralPassword` Gradle properties or
  `ORG_GRADLE_PROJECT_mavenCentralUsername` and `ORG_GRADLE_PROJECT_mavenCentralPassword` environment variables instead.
- **BREAKING**: Removed deprecated `targets` API. See README for alternative ways of adding targets.
- Behavior change: The dokka plugin is not applied by default anymore for Kotlin projects. When it is applied we will still use the dokka tasks to create the javadoc jar.
- Support for `s01.oss.sonatype.org` by setting `sonatypeHost = "S01"`.
- Introduce `com.vanniktech.maven.publish.base` plugin. This plugin contains all the functionality of the main plugin, but does not configure anything automatically.
  Instead, it offers a public API, which is also used by the main plugin to do so yourself. This allows for more flexibility and to publish different project types.
  The API is not final yet, but we're happy to receive feedback.


## [0.14.2](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.14.2) *(2021-02-14)*

- fix artifact id in Kotlin Multiplatform projects being incorrect.
- fix `closeAndReleaseRepository` requiring callers to pass `--repository`.


## [0.14.1](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.14.1) *(2021-02-11)*

- fix false positive deprecation warnings.
- fix typos in deprecation warnings.


## [0.14.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.14.0) *(2021-02-10)*

- enable publishing Kotlin/JS projects.
- fixed compatibility with Kotlin Multiplatform projects using Kotlin 1.4.30.
- fixed compatibility with Gradle plugin projects using Gradle 6.8.
- make `closeAndReleaseRepository` more flexible in choosing a repository.
- deprecated the `targets` API, check out the README on how to add more repositories.
- minimum supported Gradle version is now 6.6.


## [0.13.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.13.0) *(2020-09-07)*

- remove setting Dokka outputDirectory. [\#160](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/160) ([gabrielittner](https://github.com/gabrielittner))
- change how closeAndReleaseRepository is created to avoid ClassCastException. [\#157](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/157) ([gabrielittner](https://github.com/gabrielittner))
- Dokka 1.4 compatibility. [\#155](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/155) ([gabrielittner](https://github.com/gabrielittner))


## [0.12.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.12.0) *(2020-07-07)*

- only create one closeAndRelease task, add new property for the profile. [\#148](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/148) ([gabrielittner](https://github.com/gabrielittner))
- fix AndroidJavadocs task. [\#147](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/147) ([gabrielittner](https://github.com/gabrielittner))
- don't fail on unknown plugins. [\#142](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/142) ([gabrielittner](https://github.com/gabrielittner))
- Use POM\_INCEPTION\_YEAR gradle property. [\#140](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/140) ([benjamin-bader](https://github.com/benjamin-bader))
- cleanup after legacy was removed. [\#136](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/136) ([gabrielittner](https://github.com/gabrielittner))
- remove legacy mode. [\#135](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/135) ([gabrielittner](https://github.com/gabrielittner))
- wait for transitioning to be false before releasing. [\#133](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/133) ([gabrielittner](https://github.com/gabrielittner))

Kudos to [gabrielittner](https://github.com/gabrielittner).


## [0.11.1](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.11.1) *(2020-03-30)*

- Pom packaging is not written [\#82](https://github.com/vanniktech/gradle-maven-publish-plugin/issues/82)


## [0.11.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.11.0) *(2020-03-30)*

- Actually the same as 0.10.0 since I forgot to pull master before building :/


## [0.10.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.10.0) *(2020-03-22)*

- update mpp integrationt test. [\#124](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/124) ([gabrielittner](https://github.com/gabrielittner))
- allow to override group and version in build files. [\#123](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/123) ([gabrielittner](https://github.com/gabrielittner))
- disable legacy mode by default. [\#120](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/120) ([gabrielittner](https://github.com/gabrielittner))
- fix plugin marker pom not containing name and description. [\#119](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/119) ([gabrielittner](https://github.com/gabrielittner))
- publish empty source and javadocs jars for plugin marker. [\#118](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/118) ([gabrielittner](https://github.com/gabrielittner))
- directly support java-gradle-plugin projects. [\#115](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/115) ([gabrielittner](https://github.com/gabrielittner))
- Use 0.9.0 for publishing. [\#113](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/113) ([vanniktech](https://github.com/vanniktech))
- Update some dependencies. [\#107](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/107) ([vanniktech](https://github.com/vanniktech))

Again, huge thanks to [gabrielittner](https://github.com/gabrielittner) for all of his work in this release.


## [0.9.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.9.0) *(2020-02-08)*

- merge Utils into ProjectExtensions. [\#108](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/108) ([gabrielittner](https://github.com/gabrielittner))
- initial Kotlin Multiplatform support. [\#105](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/105) ([gabrielittner](https://github.com/gabrielittner))
- new signing property outside of targets. [\#101](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/101) ([gabrielittner](https://github.com/gabrielittner))
- fix crash on non String property types. [\#94](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/94) ([gabrielittner](https://github.com/gabrielittner))
- don't write null values to pom. [\#89](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/89) ([gabrielittner](https://github.com/gabrielittner))
- add pom developer url support. [\#88](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/88) ([gabrielittner](https://github.com/gabrielittner))
- forward Gradle test output. [\#84](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/84) ([gabrielittner](https://github.com/gabrielittner))
- initial Android support for maven-publish. [\#83](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/83) ([gabrielittner](https://github.com/gabrielittner))
- rename useMavenPublish to useLegacyMode. [\#81](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/81) ([gabrielittner](https://github.com/gabrielittner))
- More integration tests. [\#80](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/80) ([gabrielittner](https://github.com/gabrielittner))
- let integration tests run with maven publish, enable signing. [\#79](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/79) ([gabrielittner](https://github.com/gabrielittner))
- add tests for Android libraries. [\#78](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/78) ([gabrielittner](https://github.com/gabrielittner))
- fix sources and javadoc tasks not being executed. [\#77](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/77) ([gabrielittner](https://github.com/gabrielittner))
- Nexus release automation. [\#63](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/63) ([martinbonnin](https://github.com/martinbonnin))
- Require GROUP, POM\_ARTIFACT\_ID & VERSION\_NAME to be set and fail on Gradle \< 4.10.1. [\#62](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/62) ([martinbonnin](https://github.com/martinbonnin))
- Use srcDirs instead of sourceFiles to include Kotlin files to sources jar. [\#48](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/48) ([lukaville](https://github.com/lukaville))
- Switch to task-configuration avoidance. [\#46](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/46) ([ZacSweers](https://github.com/ZacSweers))

Huge thanks to [gabrielittner](https://github.com/gabrielittner) for all of his work in this release.


## [0.8.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.8.0) *(2019-02-18)*

- Change docs format for Kotlin project docs. [\#45](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/45) ([Ilya-Gh](https://github.com/Ilya-Gh))
- Add missing backticks in README.md. [\#43](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/43) ([Egorand](https://github.com/Egorand))
- Generate javadocs for Kotlin project with Dokka. [\#37](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/37) ([Ilya-Gh](https://github.com/Ilya-Gh))


## [0.7.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.7.0) *(2018-01-15)*

- Remove duplicate jar task from archives configuration. [\#39](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/39) ([marcphilipp](https://github.com/marcphilipp))
- Remove sudo: false from travis config. [\#36](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/36) ([vanniktech](https://github.com/vanniktech))
- Migrate general parts of the plugin to Kotlin. [\#35](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/35) ([gabrielittner](https://github.com/gabrielittner))
- Migrate Upload task creation to Kotlin. [\#33](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/33) ([gabrielittner](https://github.com/gabrielittner))
- Experimental implementation of Configurer that uses maven-publish. [\#32](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/32) ([gabrielittner](https://github.com/gabrielittner))
- Create interface to capsulate maven plugin specific configuration. [\#31](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/31) ([gabrielittner](https://github.com/gabrielittner))
- Improve when signing tasks run, consider all targets for signing. [\#30](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/30) ([gabrielittner](https://github.com/gabrielittner))
- Cosmetic changes. [\#26](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/26) ([vanniktech](https://github.com/vanniktech))
- Combined configuration and task creation for targets. [\#25](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/25) ([gabrielittner](https://github.com/gabrielittner))
- Reuse MavenDeployer configuration. [\#24](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/24) ([gabrielittner](https://github.com/gabrielittner))
- Add the ability to specify targets and push to multiple maven repos. [\#23](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/23) ([budius](https://github.com/budius))

Thanks to @gabrielittner @marcphilipp @budius & @WellingtonCosta for their contributions.


## [0.6.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.6.0) *(2018-09-11)*

- Configure pom of installArchives task. [\#20](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/20) ([gabrielittner](https://github.com/gabrielittner))
- Update Plugin Publish Plugin to 0.10.0. [\#19](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/19) ([vanniktech](https://github.com/vanniktech))


## [0.5.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.5.0) *(2018-08-16)*

- Add installArchives task to allow installing android library projects to local maven. [\#17](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/17) ([vanniktech](https://github.com/vanniktech))
- Fix a typo in README. [\#16](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/16) ([Egorand](https://github.com/Egorand))
- Fix typo in README.md. [\#15](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/15) ([egor-n](https://github.com/egor-n))
- fix README not actually setting properties. [\#14](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/14) ([gabrielittner](https://github.com/gabrielittner))


## [0.4.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.4.0) *(2018-06-30)*

- Remove checks for username and password. [\#12](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/12) ([vanniktech](https://github.com/vanniktech))


## [0.3.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.3.0) *(2018-06-29)*

- Make it possible to specify the release URL as a project property. [\#9](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/9) ([swankjesse](https://github.com/swankjesse))
- Package up the groovy doc in case the groovy plugin is applied. For Java plugins also add the jar archive. [\#4](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/4) ([vanniktech](https://github.com/vanniktech))
- Unify setup, improve a few things and bump versions. [\#3](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/3) ([vanniktech](https://github.com/vanniktech))


## [0.2.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.2.0) *(2018-05-26)*

- Throw exception when missing username or password only when executing the task. [\#2](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/2) ([vanniktech](https://github.com/vanniktech))


## [0.1.0](https://github.com/vanniktech/gradle-maven-publish-plugin/releases/tag/0.1.0) *(2018-05-25)*

- Initial release
