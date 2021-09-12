# Change Log

Version 0.18.0 *(2021-09-12)*
---------------------------------

- The minimum supported Kotlin version is now `1.4.30`
- It's now possible to specify `SONATYPE_HOST` as a Gradle property, e.g.
  - `SONATYPE_HOST=S01` for `s01.sonatype.org`
  - `SONATYPE_HOST=` to not add any repository by default
- Fixed an issue when publishing Kotlin MPP projects with the base plugin
- Removed checks for presence of properties that aren't used by this plugin anymore

Version 0.17.0 *(2021-07-04)*
---------------------------------

- Removed the deprecated `uploadArchives` and `installArchives` tasks. Use `publish` and `publishToMavenLocal` instead.


Version 0.16.0 *(2021-06-20)*
---------------------------------

- Add `pomFromGradleProperties` API to base plugin. This configures the pom in the same way the regular plugin does.
- Add the ability to remove the default `mavenCentral` repository, by setting `sonatypeHost` to `null`
- Support `POM_LICENSE_NAME`, `POM_LICENSE_URL` and `POM_LICENSE_DIST` properties in addition to `LICENCE` based properties.
- Fixes an issue in the base plugin that caused an error during configuration of Android projects.
- Fixes an issue with javadoc tasks when using Java toolchains.
- The deprecated `nexusOptions` and `nexus {}` methods were removed. `closeAndReleaseRepository` is automatically configured.


Version 0.15.1 *(2021-05-02)*
---------------------------------

- The `closeAndReleaseRepository` task was mistakenly expecting the wrong Gradle properties. The README and changelog also mentioned the wrong properties. The correct ones are `mavenCentralUsername` and `mavenCentralPassword` or for environment variables: `ORG_GRADLE_PROJECT_mavenCentralUsername` and `ORG_GRADLE_PROJECT_mavenCentralPassword`.
- Fix `signing` not being configurable until `afterEvaluate`
- Use empty string as in memory signing password when no password is specified
- Fix `statingProfile` in `nexusOptions` not being optional which causes an error when running `closeAndReleaseRepository`


Version 0.15.0 *(2021-04-24)*
---------------------------------

- **BREAKING**: Removed support for deprecated `RELEASE_REPOSITORY_URL`, `SNAPSHOT_REPOSITORY_URL`, `SONATYPE_NEXUS_USERNAME`, `SONATYPE_NEXUS_PASSWORD` environment variables and properties.
  For safety reasons the project will fail when finding these. Use `mavenCentralUsername` and `mavenCentralPassword` Gradle properties or
  `ORG_GRADLE_PROJECT_mavenCentralUsername` and `ORG_GRADLE_PROJECT_mavenCentralPassword` environment variables instead.
- **BREAKING**: Removed deprecated `targets` API. See README for alternative ways of adding targets.
- Behavior change: The dokka plugin is not applied by default anymore for Kotlin projects. When it is applied we will still use the dokka tasks to create the javadoc jar.
- Support for `s01.oss.sonatype.org` by setting `sonatypeHost = "S01"`.
- Introduce `com.vanniktech.maven.publish.base` plugin. This plugin contains all the functionality of the main plugin, but does not configure anything automatically.
  Instead, it offers a public API, which is also used by the main plugin to do so yourself. This allows for more flexibility and to publish different project types.
  The API is not final yet, but we're happy to receive feedback.

Version 0.14.2 *(2021-02-14)*
---------------------------------

- fix artifact id in Kotlin Multiplatform projects being incorrect
- fix `closeAndReleaseRepository` requiring callers to pass `--repository`

Version 0.14.1 *(2021-02-11)*
---------------------------------

- fix false positive deprecation warnings
- fix typos in deprecation warnings

Version 0.14.0 *(2021-02-10)*
---------------------------------

- enable publishing Kotlin/JS projects
- fixed compatibility with Kotlin Multiplatform projects using Kotlin 1.4.30
- fixed compatibility with Gradle plugin projects using Gradle 6.8
- make `closeAndReleaseRepository` more flexible in choosing a repository
- deprecated the `targets` API, check out the README on how to add more repositories
- minimum supported Gradle version is now 6.6

Version 0.13.0 *(2020-09-07)*
-----------------------------

- remove setting Dokka outputDirectory [\#160](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/160) ([gabrielittner](https://github.com/gabrielittner))
- change how closeAndReleaseRepository is created to avoid ClassCastException [\#157](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/157) ([gabrielittner](https://github.com/gabrielittner))
- Dokka 1.4 compatibility [\#155](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/155) ([gabrielittner](https://github.com/gabrielittner))

Version 0.12.0 *(2020-07-07)*
-----------------------------

- only create one closeAndRelease task, add new property for the profile [\#148](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/148) ([gabrielittner](https://github.com/gabrielittner))
- fix AndroidJavadocs task [\#147](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/147) ([gabrielittner](https://github.com/gabrielittner))
- don't fail on unknown plugins [\#142](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/142) ([gabrielittner](https://github.com/gabrielittner))
- Use POM\_INCEPTION\_YEAR gradle property [\#140](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/140) ([benjamin-bader](https://github.com/benjamin-bader))
- cleanup after legacy was removed [\#136](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/136) ([gabrielittner](https://github.com/gabrielittner))
- remove legacy mode [\#135](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/135) ([gabrielittner](https://github.com/gabrielittner))
- wait for transitioning to be false before releasing [\#133](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/133) ([gabrielittner](https://github.com/gabrielittner))

Kudos to [gabrielittner](https://github.com/gabrielittner).

Version 0.11.1 *(2020-03-30)*
-----------------------------

- Pom packaging is not written [\#82](https://github.com/vanniktech/gradle-maven-publish-plugin/issues/82)

Version 0.11.0 *(2020-03-30)*
-----------------------------

- Actually the same as 0.10.0 since I forgot to pull master before building :/

Version 0.10.0 *(2020-03-22)*
-----------------------------

- update mpp integrationt test [\#124](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/124) ([gabrielittner](https://github.com/gabrielittner))
- allow to override group and version in build files [\#123](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/123) ([gabrielittner](https://github.com/gabrielittner))
- disable legacy mode by default [\#120](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/120) ([gabrielittner](https://github.com/gabrielittner))
- fix plugin marker pom not containing name and description [\#119](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/119) ([gabrielittner](https://github.com/gabrielittner))
- publish empty source and javadocs jars for plugin marker [\#118](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/118) ([gabrielittner](https://github.com/gabrielittner))
- directly support java-gradle-plugin projects [\#115](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/115) ([gabrielittner](https://github.com/gabrielittner))
- Use 0.9.0 for publishing. [\#113](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/113) ([vanniktech](https://github.com/vanniktech))
- Update some dependencies. [\#107](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/107) ([vanniktech](https://github.com/vanniktech))

Again, huge thanks to [gabrielittner](https://github.com/gabrielittner) for all of his work in this release.

Version 0.9.0 *(2020-02-08)*
----------------------------

- merge Utils into ProjectExtensions [\#108](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/108) ([gabrielittner](https://github.com/gabrielittner))
- initial Kotlin Multiplatform support [\#105](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/105) ([gabrielittner](https://github.com/gabrielittner))
- new signing property outside of targets [\#101](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/101) ([gabrielittner](https://github.com/gabrielittner))
- fix crash on non String property types [\#94](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/94) ([gabrielittner](https://github.com/gabrielittner))
- don't write null values to pom [\#89](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/89) ([gabrielittner](https://github.com/gabrielittner))
- add pom developer url support [\#88](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/88) ([gabrielittner](https://github.com/gabrielittner))
- forward Gradle test output [\#84](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/84) ([gabrielittner](https://github.com/gabrielittner))
- initial Android support for maven-publish [\#83](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/83) ([gabrielittner](https://github.com/gabrielittner))
- rename useMavenPublish to useLegacyMode [\#81](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/81) ([gabrielittner](https://github.com/gabrielittner))
- More integration tests [\#80](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/80) ([gabrielittner](https://github.com/gabrielittner))
- let integration tests run with maven publish, enable signing [\#79](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/79) ([gabrielittner](https://github.com/gabrielittner))
- add tests for Android libraries [\#78](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/78) ([gabrielittner](https://github.com/gabrielittner))
- fix sources and javadoc tasks not being executed [\#77](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/77) ([gabrielittner](https://github.com/gabrielittner))
- Nexus release automation [\#63](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/63) ([martinbonnin](https://github.com/martinbonnin))
- Require GROUP, POM\_ARTIFACT\_ID & VERSION\_NAME to be set and fail on Gradle \< 4.10.1 [\#62](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/62) ([martinbonnin](https://github.com/martinbonnin))
- Use srcDirs instead of sourceFiles to include Kotlin files to sources jar [\#48](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/48) ([lukaville](https://github.com/lukaville))
- Switch to task-configuration avoidance [\#46](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/46) ([ZacSweers](https://github.com/ZacSweers))

Huge thanks to [gabrielittner](https://github.com/gabrielittner) for all of his work in this release.

Version 0.8.0 *(2019-02-18)*
----------------------------

- Change docs format for Kotlin project docs [\#45](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/45) ([Ilya-Gh](https://github.com/Ilya-Gh))
- Add missing backticks in README.md [\#43](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/43) ([Egorand](https://github.com/Egorand))
- Generate javadocs for Kotlin project with Dokka [\#37](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/37) ([Ilya-Gh](https://github.com/Ilya-Gh))

Version 0.7.0 *(2018-01-15)*
----------------------------

- Remove duplicate jar task from archives configuration [\#39](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/39) ([marcphilipp](https://github.com/marcphilipp))
- Remove sudo: false from travis config. [\#36](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/36) ([vanniktech](https://github.com/vanniktech))
- Migrate general parts of the plugin to Kotlin [\#35](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/35) ([gabrielittner](https://github.com/gabrielittner))
- Migrate Upload task creation to Kotlin [\#33](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/33) ([gabrielittner](https://github.com/gabrielittner))
- Experimental implementation of Configurer that uses maven-publish [\#32](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/32) ([gabrielittner](https://github.com/gabrielittner))
- Create interface to capsulate maven plugin specific configuration [\#31](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/31) ([gabrielittner](https://github.com/gabrielittner))
- Improve when signing tasks run, consider all targets for signing [\#30](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/30) ([gabrielittner](https://github.com/gabrielittner))
- Cosmetic changes. [\#26](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/26) ([vanniktech](https://github.com/vanniktech))
- Combined configuration and task creation for targets [\#25](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/25) ([gabrielittner](https://github.com/gabrielittner))
- Reuse MavenDeployer configuration [\#24](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/24) ([gabrielittner](https://github.com/gabrielittner))
- Add the ability to specify targets and push to multiple maven repos. [\#23](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/23) ([budius](https://github.com/budius))

Thanks to @gabrielittner @marcphilipp @budius & @WellingtonCosta for their contributions.

Version 0.6.0 *(2018-09-11)*
----------------------------

- Configure pom of installArchives task. [\#20](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/20) ([gabrielittner](https://github.com/gabrielittner))
- Update Plugin Publish Plugin to 0.10.0 [\#19](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/19) ([vanniktech](https://github.com/vanniktech))

Version 0.5.0 *(2018-08-16)*
----------------------------

- Add installArchives task to allow installing android library projects to local maven. [\#17](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/17) ([vanniktech](https://github.com/vanniktech))
- Fix a typo in README [\#16](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/16) ([Egorand](https://github.com/Egorand))
- Fix typo in README.md [\#15](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/15) ([egor-n](https://github.com/egor-n))
- fix README not actually setting properties [\#14](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/14) ([gabrielittner](https://github.com/gabrielittner))

Version 0.4.0 *(2018-06-30)*
----------------------------

- Remove checks for username and password. [\#12](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/12) ([vanniktech](https://github.com/vanniktech))

Version 0.3.0 *(2018-06-29)*
----------------------------

- Make it possible to specify the release URL as a project property. [\#9](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/9) ([swankjesse](https://github.com/swankjesse))
- Package up the groovy doc in case the groovy plugin is applied. For Java plugins also add the jar archive. [\#4](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/4) ([vanniktech](https://github.com/vanniktech))
- Unify setup, improve a few things and bump versions. [\#3](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/3) ([vanniktech](https://github.com/vanniktech))

Version 0.2.0 *(2018-05-26)*
----------------------------

- Throw exception when missing username or password only when executing the task. [\#2](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/2) ([vanniktech](https://github.com/vanniktech))

Version 0.1.0 *(2018-05-25)*
----------------------------

- Initial release
