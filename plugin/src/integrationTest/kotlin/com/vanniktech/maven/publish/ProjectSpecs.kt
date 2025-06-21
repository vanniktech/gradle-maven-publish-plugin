package com.vanniktech.maven.publish

import java.nio.file.Paths
import kotlin.io.path.absolute

val javaPlugin = PluginSpec("java")
val javaLibraryPlugin = PluginSpec("java-library")
val javaGradlePluginPlugin = PluginSpec("java-gradle-plugin")
val javaTestFixturesPlugin = PluginSpec("java-test-fixtures")
val javaPlatformPlugin = PluginSpec("java-platform")
val versionCatalogPlugin = PluginSpec("version-catalog")
val kotlinJvmPlugin = PluginSpec("org.jetbrains.kotlin.jvm")
val kotlinMultiplatformPlugin = PluginSpec("org.jetbrains.kotlin.multiplatform")
val kotlinAndroidPlugin = PluginSpec("org.jetbrains.kotlin.android")
val androidLibraryPlugin = PluginSpec("com.android.library")
val androidFusedLibraryPlugin = PluginSpec("com.android.fused-library")
val gradlePluginPublishPlugin = PluginSpec("com.gradle.plugin-publish")
val dokkaPlugin = PluginSpec("org.jetbrains.dokka", "1.8.10")
val dokka2Plugin = PluginSpec("org.jetbrains.dokka", "2.0.0-Beta")
val dokka2JavadocPlugin = PluginSpec("org.jetbrains.dokka-javadoc", "2.0.0-Beta")

val fixtures = Paths.get("src/integrationTest/fixtures2").absolute()

val defaultProperties = mapOf(
  "POM_NAME" to "Gradle Maven Publish Plugin Test Artifact",
  "POM_DESCRIPTION" to "Testing the Gradle Maven Publish Plugin",
  "POM_INCEPTION_YEAR" to "2018",
  "POM_URL" to "https://github.com/vanniktech/gradle-maven-publish-plugin/",
  "POM_SCM_URL" to "https://github.com/vanniktech/gradle-maven-publish-plugin/",
  "POM_SCM_CONNECTION" to "scm:git:git://github.com/vanniktech/gradle-maven-publish-plugin.git",
  "POM_SCM_DEV_CONNECTION" to "scm:git:ssh://git@github.com/vanniktech/gradle-maven-publish-plugin.git",
  "POM_LICENCE_NAME" to "The Apache Software License, Version 2.0",
  "POM_LICENCE_URL" to "https://www.apache.org/licenses/LICENSE-2.0.txt",
  "POM_LICENCE_DIST" to "repo",
  "POM_DEVELOPER_ID" to "vanniktech",
  "POM_DEVELOPER_NAME" to "Niklas Baudy",
  "POM_DEVELOPER_URL" to "https://github.com/vanniktech/",
)

fun javaProjectSpec() = ProjectSpec(
  plugins = listOf(
    javaPlugin,
  ),
  group = "com.example",
  artifactId = "test-artifact",
  version = "1.0.0",
  properties = defaultProperties,
  sourceFiles = listOf(
    SourceFile("main", "java", "com/vanniktech/maven/publish/test/JavaTestClass.java"),
  ),
  basePluginConfig = "configure(new JavaLibrary(new JavadocJar.Empty(), true))",
)

fun javaLibraryProjectSpec() = ProjectSpec(
  plugins = listOf(
    javaLibraryPlugin,
  ),
  group = "com.example",
  artifactId = "test-artifact",
  version = "1.0.0",
  properties = defaultProperties,
  sourceFiles = listOf(
    SourceFile("main", "java", "com/vanniktech/maven/publish/test/JavaTestClass.java"),
  ),
  basePluginConfig = "configure(new JavaLibrary(new JavadocJar.Empty(), true))",
)

fun javaGradlePluginProjectSpec() = ProjectSpec(
  plugins = listOf(
    javaGradlePluginPlugin,
  ),
  group = "com.example",
  artifactId = "test-artifact",
  version = "1.0.0",
  properties = defaultProperties,
  sourceFiles = listOf(
    SourceFile("main", "java", "com/vanniktech/maven/publish/test/JavaTestClass.java"),
  ),
  buildFileExtra =
    """
    gradlePlugin {
        plugins {
            mavenPublishPlugin {
                // the id here should be different from the group id and artifact id
                id = 'com.example.test-plugin'
                implementationClass = 'com.vanniktech.maven.publish.test.TestPlugin'
            }
        }
    }
    """.trimIndent(),
  basePluginConfig = "configure(new GradlePlugin(new JavadocJar.Empty(), true))",
)

fun javaGradlePluginWithGradlePluginPublish(gradlePluginPublish: GradlePluginPublish): ProjectSpec {
  val base = javaGradlePluginProjectSpec()
  return base.copy(
    plugins = base.plugins + gradlePluginPublishPlugin.copy(version = gradlePluginPublish.version),
    basePluginConfig = "configure(new GradlePublishPlugin())",
  )
}

fun javaGradlePluginKotlinProjectSpec(version: KotlinVersion): ProjectSpec {
  val plainJavaGradlePluginProject = javaGradlePluginProjectSpec()
  return plainJavaGradlePluginProject.copy(
    plugins = plainJavaGradlePluginProject.plugins + kotlinJvmPlugin.copy(version = version.value),
    sourceFiles = plainJavaGradlePluginProject.sourceFiles + listOf(
      SourceFile("main", "kotlin", "com/vanniktech/maven/publish/test/KotlinTestClass.kt"),
    ),
  )
}

fun kotlinJvmProjectSpec(version: KotlinVersion) = ProjectSpec(
  plugins = listOf(
    kotlinJvmPlugin.copy(version = version.value),
  ),
  group = "com.example",
  artifactId = "test-artifact",
  version = "1.0.0",
  properties = defaultProperties,
  sourceFiles = listOf(
    SourceFile("main", "java", "com/vanniktech/maven/publish/test/JavaTestClass.java"),
    SourceFile("main", "kotlin", "com/vanniktech/maven/publish/test/KotlinTestClass.kt"),
  ),
  basePluginConfig = "configure(new KotlinJvm(new JavadocJar.Empty(), true))",
)

fun kotlinMultiplatformProjectSpec(version: KotlinVersion) = ProjectSpec(
  plugins = listOf(
    kotlinMultiplatformPlugin.copy(version = version.value),
  ),
  group = "com.example",
  artifactId = "test-artifact",
  version = "1.0.0",
  properties = defaultProperties,
  sourceFiles = listOf(
    SourceFile("commonMain", "kotlin", "com/vanniktech/maven/publish/test/ExpectedTestClass.kt"),
    SourceFile("jvmMain", "kotlin", "com/vanniktech/maven/publish/test/ExpectedTestClass.kt"),
    SourceFile("linuxMain", "kotlin", "com/vanniktech/maven/publish/test/ExpectedTestClass.kt"),
    SourceFile("nodeJsMain", "kotlin", "com/vanniktech/maven/publish/test/ExpectedTestClass.kt"),
  ),
  basePluginConfig = "configure(new KotlinMultiplatform(new JavadocJar.Empty()))",
  buildFileExtra =
    """
    kotlin {
        jvm()
        js("nodeJs", "IR") {
            nodejs()
        }
        linuxX64("linux")

        sourceSets {
            commonMain {
                dependencies {
                }
            }
            jvmMain {
                dependencies {
                }
            }
            nodeJsMain {
                dependencies {
                }
            }
            linuxMain {
                dependencies {
                }
            }
        }
    }
    """.trimIndent(),
)

fun kotlinMultiplatformWithAndroidLibraryProjectSpec(agpVersion: AgpVersion, kotlinVersion: KotlinVersion): ProjectSpec {
  val baseProject = kotlinMultiplatformProjectSpec(kotlinVersion)
  return baseProject.copy(
    plugins = listOf(androidLibraryPlugin.copy(version = agpVersion.value)) + baseProject.plugins,
    sourceFiles = baseProject.sourceFiles + listOf(
      SourceFile("androidMain", "kotlin", "com/vanniktech/maven/publish/test/AndroidTestClass.kt"),
      SourceFile("androidDebug", "kotlin", "com/vanniktech/maven/publish/test/ExpectedTestClass.kt"),
      SourceFile("androidRelease", "kotlin", "com/vanniktech/maven/publish/test/ExpectedTestClass.kt"),
    ),
    // needs to explicitly specify release to match the main plugin default behavior
    basePluginConfig = "configure(new KotlinMultiplatform(new JavadocJar.Empty(), true, [\"release\"]))",
    buildFileExtra = baseProject.buildFileExtra +
      """

      android {
        compileSdkVersion = 31
        namespace = "com.test"

        // resolves config cache issue, can be removed when AGP 8 becomes the minimum supported version
        buildFeatures {
            shaders = false
        }
      }

      kotlin {
        androidTarget {}

        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of("8"))
        }
      }
      """.trimIndent(),
  )
}

fun kotlinMultiplatformWithAndroidLibraryAndSpecifiedVariantsProjectSpec(
  agpVersion: AgpVersion,
  kotlinVersion: KotlinVersion,
): ProjectSpec {
  val baseProject = kotlinMultiplatformWithAndroidLibraryProjectSpec(agpVersion, kotlinVersion)
  return baseProject.copy(
    basePluginConfig = "configure(new KotlinMultiplatform(new JavadocJar.Empty()))",
    buildFileExtra = baseProject.buildFileExtra +
      """

      kotlin {
        androidTarget {
          publishLibraryVariants("release", "debug")
        }
      }
      """.trimIndent(),
  )
}

fun androidLibraryProjectSpec(version: AgpVersion) = ProjectSpec(
  plugins = listOf(
    androidLibraryPlugin.copy(version = version.value),
  ),
  group = "com.example",
  artifactId = "test-artifact",
  version = "1.0.0",
  properties = defaultProperties,
  sourceFiles = listOf(
    SourceFile("main", "java", "com/vanniktech/maven/publish/test/JavaTestClass.java"),
  ),
  basePluginConfig = "configure(new AndroidSingleVariantLibrary(\"release\", true, true))",
  buildFileExtra =
    """
    android {
        namespace "com.test.library"
        compileSdkVersion 29

        // resolves config cache issue, can be removed when AGP 8 becomes the minimum supported version
        buildFeatures {
            shaders = false
        }
    }

    // disable the dokka task to speed up Android tests significantly
    afterEvaluate {
      tasks.named("javaDocReleaseGeneration").configure {
        it.enabled = false
      }
    }
    """.trimIndent(),
)

fun androidLibraryKotlinProjectSpec(agpVersion: AgpVersion, kotlinVersion: KotlinVersion): ProjectSpec {
  val plainAndroidProject = androidLibraryProjectSpec(agpVersion)
  return plainAndroidProject.copy(
    plugins = plainAndroidProject.plugins + kotlinAndroidPlugin.copy(version = kotlinVersion.value),
    sourceFiles = plainAndroidProject.sourceFiles + listOf(
      SourceFile("main", "kotlin", "com/vanniktech/maven/publish/test/KotlinTestClass.kt"),
    ),
    buildFileExtra = plainAndroidProject.buildFileExtra +
      """

      kotlin {
          jvmToolchain {
              languageVersion.set(JavaLanguageVersion.of("8"))
          }
      }
      """.trimIndent(),
  )
}

fun androidFusedLibraryProjectSpec(version: AgpVersion) = ProjectSpec(
  plugins = listOf(
    androidFusedLibraryPlugin.copy(version = version.value),
  ),
  group = "com.example",
  artifactId = "test-artifact",
  version = "1.0.0",
  properties = defaultProperties,
  sourceFiles = listOf(
    SourceFile("main", "java", "com/vanniktech/maven/publish/test/JavaTestClass.java"),
  ),
  basePluginConfig = "configure(new AndroidFusedLibrary())",
  buildFileExtra = """
    androidFusedLibrary {
        namespace = "com.test.library"
        minSdk = 29
    }
  """.trimIndent(),
)

fun javaPlatformProjectSpec() = ProjectSpec(
  plugins = listOf(
    javaPlatformPlugin,
  ),
  group = "com.example",
  artifactId = "test-artifact",
  version = "1.0.0",
  properties = defaultProperties,
  sourceFiles = emptyList(),
  basePluginConfig = "configure(new JavaPlatform())",
  buildFileExtra =
    """
    dependencies {
        constraints {
            api 'commons-httpclient:commons-httpclient:3.1'
            runtime 'org.postgresql:postgresql:42.2.5'
        }
    }
    """.trimIndent(),
)

fun versionCatalogProjectSpec() = ProjectSpec(
  plugins = listOf(
    versionCatalogPlugin,
  ),
  group = "com.example",
  artifactId = "test-artifact",
  version = "1.0.0",
  properties = defaultProperties,
  sourceFiles = emptyList(),
  basePluginConfig = "configure(new VersionCatalog())",
  buildFileExtra =
    """
    catalog {
        versionCatalog {
            library('my-lib', 'com.mycompany:mylib:1.2')
        }
    }
    """.trimIndent(),
)
