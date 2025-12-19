package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.AgpVersion.Companion.AGP_9_0_0
import com.vanniktech.maven.publish.IntegrationTestBuildConfig.DOKKA_STABLE
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
val androidMultiplatformLibraryPlugin = PluginSpec("com.android.kotlin.multiplatform.library")
val androidFusedLibraryPlugin = PluginSpec("com.android.fused-library")
val gradlePluginPublishPlugin = PluginSpec("com.gradle.plugin-publish")
val dokkaPlugin = PluginSpec("org.jetbrains.dokka", DOKKA_STABLE)
val dokkaJavadocPlugin = PluginSpec("org.jetbrains.dokka-javadoc", DOKKA_STABLE)

val fixtures = Paths.get("src/integrationTest/fixtures2").absolute()

val defaultProperties = mapOf(
  "POM_NAME" to "Gradle Maven Publish Plugin Test Artifact",
  "POM_DESCRIPTION" to "Testing the Gradle Maven Publish Plugin",
  "POM_INCEPTION_YEAR" to "2018",
  "POM_URL" to "https://github.com/vanniktech/gradle-maven-publish-plugin/",
  "POM_SCM_URL" to "https://github.com/vanniktech/gradle-maven-publish-plugin/",
  "POM_SCM_CONNECTION" to "scm:git:git://github.com/vanniktech/gradle-maven-publish-plugin.git",
  "POM_SCM_DEV_CONNECTION" to "scm:git:ssh://git@github.com/vanniktech/gradle-maven-publish-plugin.git",
  "POM_LICENCE_NAME" to "Apache-2.0",
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
  basePluginConfig = "configure(new JavaLibrary(new JavadocJar.Empty()))",
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
  basePluginConfig = "configure(new JavaLibrary(new JavadocJar.Empty()))",
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
  basePluginConfig = "configure(new GradlePlugin(new JavadocJar.Empty()))",
)

fun javaGradlePluginWithGradlePluginPublish(gradlePluginPublish: GradlePluginPublish): ProjectSpec {
  val base = javaGradlePluginProjectSpec()
  return base.copy(
    plugins = base.plugins + gradlePluginPublishPlugin.copy(version = gradlePluginPublish.value),
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
  basePluginConfig = "configure(new KotlinJvm(new JavadocJar.Empty()))",
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
    SourceFile("linuxX64Main", "kotlin", "com/vanniktech/maven/publish/test/ExpectedTestClass.kt"),
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
        linuxX64()

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
            linuxX64Main {
                dependencies {
                }
            }
        }
    }
    """.trimIndent(),
)

// TODO remove when min AGP version is AGP 9
fun kotlinMultiplatformWithAndroidLibraryProjectSpec(agpVersion: AgpVersion, kotlinVersion: KotlinVersion): ProjectSpec {
  val baseProject = kotlinMultiplatformProjectSpec(kotlinVersion)
  return baseProject.copy(
    plugins = baseProject.plugins + listOf(androidLibraryPlugin.copy(version = agpVersion.value)),
    sourceFiles = baseProject.sourceFiles + listOf(
      SourceFile("androidMain", "kotlin", "com/vanniktech/maven/publish/test/AndroidTestClass.kt"),
      SourceFile("androidDebug", "kotlin", "com/vanniktech/maven/publish/test/ExpectedTestClass.kt"),
      SourceFile("androidRelease", "kotlin", "com/vanniktech/maven/publish/test/ExpectedTestClass.kt"),
    ),
    // needs to explicitly specify release to match the main plugin default behavior
    basePluginConfig = "configure(new KotlinMultiplatform(new JavadocJar.Empty(), SourcesJar.Sources.INSTANCE, [\"release\"]))",
    buildFileExtra = baseProject.buildFileExtra +
      """

      android {
        compileSdk = 34
        namespace = "com.test.library"
      }

      kotlin {
        androidTarget {}

        jvmToolchain(11)
      }
      """.trimIndent(),
    propertiesExtra =
      """
      android.builtInKotlin=false
      android.newDsl=false
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

fun kotlinMultiplatformWithModernAndroidLibraryProjectSpec(agpVersion: AgpVersion, kotlinVersion: KotlinVersion): ProjectSpec {
  val baseProject = kotlinMultiplatformProjectSpec(kotlinVersion)
  return baseProject.copy(
    plugins = baseProject.plugins + listOf(androidMultiplatformLibraryPlugin.copy(version = agpVersion.value)),
    sourceFiles = baseProject.sourceFiles + listOf(
      SourceFile("androidMain", "kotlin", "com/vanniktech/maven/publish/test/AndroidTestClass.kt"),
      SourceFile("androidMain", "kotlin", "com/vanniktech/maven/publish/test/ExpectedTestClass.kt"),
    ),
    buildFileExtra =
      """
      kotlin {
          androidLibrary {
              compileSdk = 36
              namespace = "com.example.namespace"
          }
      }

      """.trimIndent() + baseProject.buildFileExtra,
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
  basePluginConfig = "configure(new AndroidSingleVariantLibrary(new JavadocJar.Javadoc(), SourcesJar.Sources.INSTANCE, 'release'))",
  buildFileExtra =
    """
    android {
        compileSdk = 34
        namespace = "com.test.library"
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
  val plugins = if (agpVersion >= AGP_9_0_0) {
    plainAndroidProject.plugins
  } else {
    plainAndroidProject.plugins + kotlinAndroidPlugin.copy(version = kotlinVersion.value)
  }
  return plainAndroidProject.copy(
    plugins = plugins,
    sourceFiles = plainAndroidProject.sourceFiles + listOf(
      SourceFile("main", "kotlin", "com/vanniktech/maven/publish/test/KotlinTestClass.kt"),
    ),
    buildFileExtra = plainAndroidProject.buildFileExtra +
      """

      kotlin {
        jvmToolchain(11)
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
  sourceFiles = emptyList(),
  basePluginConfig = "configure(new AndroidFusedLibrary())",
  // TODO remove old min sdk syntax when min AGP version is 9
  buildFileExtra =
    """
    androidFusedLibrary {
        namespace = "com.test.library"
        ${if (version >= AGP_9_0_0) "minSdk { version = release(34) }" else "minSdk = 29" }
    }
    """.trimIndent(),
  // TODO remove when stable
  propertiesExtra =
    """
    android.experimental.fusedLibrarySupport=true
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
