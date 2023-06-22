package com.vanniktech.maven.publish

import org.apache.maven.model.Dependency
import org.apache.maven.model.DependencyManagement
import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import org.apache.maven.model.Scm

data class PomDependency(
  val groupId: String,
  val artifactId: String,
  val version: String,
  val scope: String?,
  val optional: Boolean? = null,
)

fun kotlinStdlibCommon(version: KotlinVersion) = PomDependency("org.jetbrains.kotlin", "kotlin-stdlib-common", version.value, "compile")
fun kotlinStdlibJdk(version: KotlinVersion) = PomDependency("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", version.value, "compile")
fun kotlinStdlibJs(version: KotlinVersion) = PomDependency("org.jetbrains.kotlin", "kotlin-stdlib-js", version.value, "compile")
fun kotlinDomApi(version: KotlinVersion) = PomDependency("org.jetbrains.kotlin", "kotlin-dom-api-compat", version.value, "compile")

fun createPom(
  groupId: String,
  artifactId: String,
  version: String,
  packaging: String?,
  dependencies: List<PomDependency>,
  dependencyManagementDependencies: List<PomDependency>,
): Model {
  val model = createMinimalPom(groupId, artifactId, version, packaging, dependencies, dependencyManagementDependencies)

  model.name = "Gradle Maven Publish Plugin Test Artifact"
  model.description = "Testing the Gradle Maven Publish Plugin"
  model.url = "https://github.com/vanniktech/gradle-maven-publish-plugin/"
  model.inceptionYear = "2018"
  model.addLicense(
    License().apply {
      name = "The Apache Software License, Version 2.0"
      url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      distribution = "repo"
    },
  )
  model.addDeveloper(
    Developer().apply {
      id = "vanniktech"
      name = "Niklas Baudy"
      url = "https://github.com/vanniktech/"
    },
  )
  model.scm = Scm().apply {
    connection = "scm:git:git://github.com/vanniktech/gradle-maven-publish-plugin.git"
    developerConnection = "scm:git:ssh://git@github.com/vanniktech/gradle-maven-publish-plugin.git"
    url = "https://github.com/vanniktech/gradle-maven-publish-plugin/"
  }

  return model
}

fun createMinimalPom(
  groupId: String,
  artifactId: String,
  version: String,
  packaging: String?,
  dependencies: List<PomDependency>,
  dependencyManagementDependencies: List<PomDependency>,
): Model {
  val model = Model()
  model.modelVersion = "4.0.0"
  model.modelEncoding = "UTF-8"
  model.groupId = groupId
  model.artifactId = artifactId
  model.version = version
  if (packaging != null) {
    model.packaging = packaging
  }
  dependencies.forEach {
    model.addDependency(
      Dependency().apply {
        this.groupId = it.groupId
        this.artifactId = it.artifactId
        this.version = it.version
        this.scope = it.scope
        if (it.optional != null) {
          this.isOptional = it.optional
        }
      },
    )
  }

  if (dependencyManagementDependencies.isNotEmpty()) {
    model.dependencyManagement = DependencyManagement().apply {
      dependencyManagementDependencies.forEach {
        addDependency(
          Dependency().apply {
            this.groupId = it.groupId
            this.artifactId = it.artifactId
            this.version = it.version
            this.scope = it.scope
            if (it.optional != null) {
              this.isOptional = it.optional
            }
          },
        )
      }
    }
  }

  return model
}
