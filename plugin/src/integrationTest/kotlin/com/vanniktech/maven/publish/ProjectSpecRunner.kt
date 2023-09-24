package com.vanniktech.maven.publish

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import org.gradle.testkit.runner.GradleRunner

fun ProjectSpec.run(fixtures: Path, temp: Path, options: TestOptions): ProjectResult {
  val project = temp.resolve("project").apply { createDirectories() }
  val repo = temp.resolve("repo").apply { createDirectories() }

  writeBuildFile(project.resolve("build.gradle"), repo, options)
  writeSettingFile(project.resolve("settings.gradle"))
  writeGradleProperties(project.resolve("gradle.properties"), options)
  writeSourceFiles(fixtures, project)
  fixtures.resolve("test-secring.gpg").copyTo(project.resolve("test-secring.gpg"))

  val task = ":publishAllPublicationsToTestFolderRepository"
  val arguments = mutableListOf(task, "--stacktrace")
  if (options.supportsConfigCaching(plugins)) {
    arguments.add("--configuration-cache")
  }

  val result = GradleRunner.create()
    .withGradleVersion(options.gradleVersion.value)
    .withProjectDir(project.toFile())
    .withDebug(true)
    .withArguments(arguments)
    .build()

  return ProjectResult(
    result = result,
    task = task,
    projectSpec = this,
    project = project,
    repo = repo,
  )
}

private fun TestOptions.supportsConfigCaching(plugins: List<PluginSpec>): Boolean {
  // Kotlin MPP supports config cache since 1.9.0
  val multiplatformPlugin = plugins.find { it.id == kotlinMultiplatformPlugin.id }
  if (multiplatformPlugin != null) {
    val version = KotlinVersion.entries.find { it.value == multiplatformPlugin.version }!!
    return version >= KotlinVersion.KT_1_9_0
  }

  // TODO https://github.com/Kotlin/dokka/issues/2231
  if (plugins.any { it.id == dokkaPlugin.id }) {
    return false
  }

  return gradleVersion >= GradleVersion.GRADLE_8_1 || signing == TestOptions.Signing.NO_SIGNING
}

private fun ProjectSpec.writeBuildFile(path: Path, repo: Path, options: TestOptions) {
  path.writeText(
    """
    import com.vanniktech.maven.publish.*

    ${pluginsBlock(options)}

    ${publishingBlock(options)}

    publishing {
      repositories {
        maven {
          name = "testFolder"
          url = "${repo.toAbsolutePath()}"
        }
      }
    }

    $buildFileExtra

    """.trimIndent(),
  )
}

private fun ProjectSpec.pluginsBlock(options: TestOptions) = buildString {
  appendLine("plugins {")
  plugins.forEach {
    append("  id \"${it.id}\"")

    if (it.version != null) {
      appendLine(" version \"${it.version}\"")
    } else {
      appendLine()
    }
  }

  val pluginVersion = System.getProperty("com.vanniktech.publish.version")
  when (options.config) {
    TestOptions.Config.BASE -> appendLine(" id \"com.vanniktech.maven.publish.base\" version \"${pluginVersion}\"")
    TestOptions.Config.DSL,
    TestOptions.Config.PROPERTIES,
    -> appendLine(" id \"com.vanniktech.maven.publish\" version \"${pluginVersion}\"")
  }

  appendLine("}")
}

private fun ProjectSpec.publishingBlock(options: TestOptions): String {
  return when (options.config) {
    TestOptions.Config.PROPERTIES -> {
      """
        mavenPublishing {
        }
      """.trimIndent()
    }
    TestOptions.Config.BASE,
    TestOptions.Config.DSL,
    -> listOfNotNull(
      """

       mavenPublishing {
         ${if (options.config == TestOptions.Config.BASE) basePluginConfig else ""}
         ${if (options.signing != TestOptions.Signing.NO_SIGNING) "signAllPublications()" else ""}

         ${if (group != null && artifactId != null && version != null) "coordinates(\"$group\", \"$artifactId\", \"$version\")" else ""}

         pom {
      """,
      "    name = \"${properties["POM_NAME"]}\"".takeIf { properties.containsKey("POM_NAME") },
      "    description = \"${properties["POM_DESCRIPTION"]}\"".takeIf { properties.containsKey("POM_DESCRIPTION") },
      "    inceptionYear = \"${properties["POM_INCEPTION_YEAR"]}\"".takeIf { properties.containsKey("POM_INCEPTION_YEAR") },
      "    url = \"${properties["POM_URL"]}\"".takeIf { properties.containsKey("POM_URL") },
      """
            licenses {
              license {
                name = "${properties["POM_LICENCE_NAME"]}"
                url = "${properties["POM_LICENCE_URL"]}"
                distribution = "${properties["POM_LICENCE_DIST"]}"
              }
            }
      """.trimIndent().takeIf { properties.containsKey("POM_LICENCE_NAME") },
      """
            developers {
              developer {
                id = "${properties["POM_DEVELOPER_ID"]}"
                name = "${properties["POM_DEVELOPER_NAME"]}"
                url = "${properties["POM_DEVELOPER_URL"]}"
              }
            }
      """.trimIndent().takeIf { properties.containsKey("POM_DEVELOPER_ID") },
      """
            scm {
              url = "${properties["POM_SCM_URL"]}"
              connection = "${properties["POM_SCM_CONNECTION"]}"
              developerConnection = "${properties["POM_SCM_DEV_CONNECTION"]}"
            }
      """.trimIndent().takeIf { properties.containsKey("POM_SCM_URL") },
      """
         }
       }
      """.trimIndent(),
    ).joinToString(separator = "\n")
  }
}

private fun writeSettingFile(path: Path) {
  path.writeText(
    """
    pluginManagement {
        repositories {
            mavenLocal()
            mavenCentral()
            google()
            gradlePluginPortal()
        }
    }

    dependencyResolutionManagement {
        repositories {
            mavenLocal()
            mavenCentral()
            google()
        }
    }

    rootProject.name = "default-root-project-name"

    """.trimIndent(),
  )
}

private fun ProjectSpec.writeGradleProperties(path: Path, options: TestOptions) {
  path.writeText(
    buildString {
      appendLine("org.gradle.vfs.watch=false")
      appendLine("kotlin.compiler.execution.strategy=in-process")
      appendLine("kotlin.mpp.androidSourceSetLayoutVersion1.nowarn=true")
      appendLine()

      if (options.config == TestOptions.Config.PROPERTIES) {
        if (group != null) {
          appendLine("GROUP=$group")
        }
        if (artifactId != null) {
          appendLine("POM_ARTIFACT_ID=$artifactId")
        }
        if (version != null) {
          appendLine("VERSION_NAME=$version")
        }
        appendLine()

        properties.forEach {
          appendLine("${it.key}=${it.value}")
        }

        appendLine()
        when (options.signing) {
          TestOptions.Signing.NO_SIGNING -> {}
          TestOptions.Signing.GPG_KEY -> {
            appendLine("RELEASE_SIGNING_ENABLED=true")
          }
          TestOptions.Signing.IN_MEMORY_KEY -> {
            appendLine("RELEASE_SIGNING_ENABLED=true")
          }
        }
      }

      when (options.signing) {
        TestOptions.Signing.NO_SIGNING -> {}
        TestOptions.Signing.GPG_KEY -> {
          appendLine("signing.keyId=B89C4055")
          appendLine("signing.password=test")
          appendLine("signing.secretKeyRingFile=test-secring.gpg")
        }
        TestOptions.Signing.IN_MEMORY_KEY -> {
          appendLine("signingInMemoryKey=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")
          appendLine("signingInMemoryKeyId=B89C4055")
          appendLine("signingInMemoryKeyPassword=test")
        }
      }
    },
  )
}

private fun ProjectSpec.writeSourceFiles(fixtures: Path, target: Path) {
  sourceFiles.forEach {
    Files.createDirectories(it.resolveIn(target).parent)
    Files.copy(it.resolveIn(fixtures), it.resolveIn(target))
  }
}
