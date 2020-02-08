# Change Log

Version 0.10.0 *(In development)*
---------------------------------

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