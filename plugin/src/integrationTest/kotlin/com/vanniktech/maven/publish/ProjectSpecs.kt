package com.vanniktech.maven.publish

val javaPlugin = PluginSpec("java")
val javaLibraryPlugin = PluginSpec("java-library")
val javaGradlePluginPlugin = PluginSpec("java-gradle-plugin")
val kotlinJvmPlugin = PluginSpec("org.jetbrains.kotlin.jvm")
val kotlinMultiplatformPlugin = PluginSpec("org.jetbrains.kotlin.multiplatform")
val kotlinJsPlugin = PluginSpec("org.jetbrains.kotlin.js")
val kotlinAndroidPlugin = PluginSpec("org.jetbrains.kotlin.android")
val androidLibraryPlugin = PluginSpec("com.android.library")

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
  )
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
  )
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
  buildFileExtra = """
    gradlePlugin {
        plugins {
            mavenPublishPlugin {
                // the id here should be different from the group id and artifact id
                id = 'com.example.test-plugin'
                implementationClass = 'com.vanniktech.maven.publish.test.TestPlugin'
            }
        }
    }
  """.trimIndent()
)

fun kotlinJvmProjectSpec(version: KotlinVersion) = ProjectSpec(
  plugins = listOf(
    kotlinJvmPlugin.copy(version = version.value)
  ),
  group = "com.example",
  artifactId = "test-artifact",
  version = "1.0.0",
  properties = defaultProperties,
  sourceFiles = listOf(
    SourceFile("main", "java", "com/vanniktech/maven/publish/test/JavaTestClass.java"),
    SourceFile("main", "kotlin", "com/vanniktech/maven/publish/test/KotlinTestClass.kt"),
  )
)

fun kotlinJsProjectSpec(version: KotlinVersion) = ProjectSpec(
  plugins = listOf(
    kotlinJsPlugin.copy(version = version.value)
  ),
  group = "com.example",
  artifactId = "test-artifact",
  version = "1.0.0",
  properties = defaultProperties,
  sourceFiles = listOf(
    SourceFile("main", "kotlin", "com/vanniktech/maven/publish/test/KotlinTestClass.kt"),
  ),
  buildFileExtra = """
    kotlin {
        js("IR") {
            nodejs()
        }
    }
  """.trimIndent()
)

fun androidLibraryProjectSpec(version: AgpVersion) = ProjectSpec(
  plugins = listOf(
    androidLibraryPlugin.copy(version = version.value)
  ),
  group = "com.example",
  artifactId = "test-artifact",
  version = "1.0.0",
  properties = defaultProperties,
  sourceFiles = listOf(
    SourceFile("main", "java", "com/vanniktech/maven/publish/test/JavaTestClass.java"),
  ),
  buildFileExtra = """
    android {
        namespace "com.test.library"
        compileSdkVersion 29
    }

    // disable the dokka task to speed up Android tests significantly
    afterEvaluate {
      tasks.named("javaDocReleaseGeneration").configure {
        it.enabled = false
      }
    }
  """.trimIndent()
)

fun androidLibraryKotlinProjectSpec(agpVersion: AgpVersion, kotlinVersion: KotlinVersion): ProjectSpec {
  val plainAndroidProject = androidLibraryProjectSpec(agpVersion)
  return plainAndroidProject.copy(
    plugins = plainAndroidProject.plugins + kotlinAndroidPlugin.copy(version = kotlinVersion.value),
    sourceFiles = plainAndroidProject.sourceFiles + listOf(
      SourceFile("main", "kotlin", "com/vanniktech/maven/publish/test/KotlinTestClass.kt")
    ),
    buildFileExtra = plainAndroidProject.buildFileExtra + """

      kotlin {
          jvmToolchain {
              languageVersion.set(JavaLanguageVersion.of("8"))
          }
      }
    """.trimIndent()
  )
}
