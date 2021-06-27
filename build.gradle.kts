buildscript {
  val kotlinVersion = "1.4.32"

  // Save the kotlin version in the project extra properties, so we can reuse it later
  extra["kotlinVersion"] = kotlinVersion

  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
  }

  dependencies {
    classpath(kotlin("gradle-plugin", version = kotlinVersion))
    classpath("com.github.ben-manes:gradle-versions-plugin:0.38.0")
    classpath("org.jlleitschuh.gradle:ktlint-gradle:10.0.0")
    classpath("com.vanniktech:gradle-maven-publish-plugin:0.16.0-rc1")
  }
}

plugins {
  id("java-library")
  id("java-gradle-plugin")
}
apply(plugin = "kotlin")
apply(plugin = "kotlin-kapt")
apply(plugin = "com.github.ben-manes.versions")
apply(plugin = "org.jlleitschuh.gradle.ktlint")
apply(plugin = "com.vanniktech.maven.publish")

object Version {
  const val junit = "4.13.2"
  const val assertj = "3.19.0"
  const val agp = "3.6.0"
  const val dokka = "0.9.18"
  const val retrofit = "2.9.0"
  const val moshi = "1.12.0"
}

// Retrieve the kotlinVersion from the buildscript block
val kotlinVersion = project.extra["kotlinVersion"]

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
  version.set("0.41.0")
}

configure<GradlePluginDevelopmentExtension> {
  plugins {
    create("mavenPublishPlugin") {
      id = "com.vanniktech.maven.publish"
      implementationClass = "com.vanniktech.maven.publish.MavenPublishPlugin"
    }
    create("mavenPublishBasePlugin") {
      id = "com.vanniktech.maven.publish.base"
      implementationClass = "com.vanniktech.maven.publish.MavenPublishBasePlugin"
    }
  }
}

repositories {
  mavenCentral()
  google()
}

dependencies {
  api(gradleApi())
  api(kotlin("stdlib", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))

  compileOnly("org.jetbrains.dokka:dokka-gradle-plugin:${Version.dokka}")
  compileOnly("org.jetbrains.dokka:dokka-android-gradle-plugin:${Version.dokka}")
  compileOnly(kotlin("gradle-plugin", version = "$kotlinVersion"))
  compileOnly("com.android.tools.build:gradle:${Version.agp}")

  add("kapt", "com.squareup.moshi:moshi-kotlin-codegen:${Version.moshi}")

  implementation("com.squareup.moshi:moshi:${Version.moshi}")
  implementation("com.squareup.retrofit2:retrofit:${Version.retrofit}")
  implementation("com.squareup.retrofit2:converter-moshi:${Version.retrofit}")

  testImplementation(gradleTestKit())
  testImplementation("junit:junit:${Version.junit}")
  testImplementation("org.assertj:assertj-core:${Version.assertj}")
  testImplementation("com.github.stefanbirkner:system-rules:1.19.0")
  testImplementation(kotlin("reflect", version = "${project.extra["kotlinVersion"]}"))
  // for non test kit tests
  testImplementation("com.android.tools.build:gradle:${Version.agp}")
  testImplementation(kotlin("gradle-plugin", version = "${project.extra["kotlinVersion"]}"))
  testImplementation("org.jetbrains.dokka:dokka-gradle-plugin:${Version.dokka}")
  testImplementation("org.jetbrains.dokka:dokka-android-gradle-plugin:${Version.dokka}")
}

tasks.withType(PluginUnderTestMetadata::class.java).configureEach {
  // for test kit tests
  pluginClasspath.from(configurations.compileOnly)
}

sourceSets {
  named("test") {
    this.withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
      kotlin.srcDirs(listOf("src/integrationTest/kotlin"))
    }
  }
}

java.sourceCompatibility = JavaVersion.VERSION_1_7

tasks.named("wrapper") {
  this as Wrapper
  gradleVersion = "6.8.3"
  distributionType = Wrapper.DistributionType.BIN
}

configurations.all {
  resolutionStrategy {
    force("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    force("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
  }
}

tasks.named("test") {
  this as Test
  testLogging {
    events("passed", "skipped", "failed")
    setExceptionFormat("full")
  }
}
