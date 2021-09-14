plugins {
  id("shared")
  `java-gradle-plugin`
}

configure<GradlePluginDevelopmentExtension> {
  plugins {
    create("mavenPublishPlugin") {
      id = "com.vanniktech.maven.publish"
      implementationClass = "com.vanniktech.maven.publish.MavenPublishPlugin"
      displayName = "Gradle Maven Publish Plugin"
      description = "Gradle plugin that configures publish tasks to automatically upload all of your Java, Kotlin, Gradle, or Android libraries to any Maven instance."
    }
    create("mavenPublishBasePlugin") {
      id = "com.vanniktech.maven.publish.base"
      implementationClass = "com.vanniktech.maven.publish.MavenPublishBasePlugin"
      displayName = "Gradle Maven Publish Base Plugin"
      description = "Gradle plugin that configures publish tasks to automatically upload all of your Java, Kotlin, Gradle, or Android libraries to any Maven instance."
    }
  }
}

dependencies {
  api(gradleApi())
  api(kotlin("stdlib"))

  compileOnly("org.jetbrains.dokka:dokka-gradle-plugin:${Version.dokka}")
  compileOnly(kotlin("gradle-plugin"))
  compileOnly("com.android.tools.build:gradle:${Version.agp}")

  add("kapt", "com.squareup.moshi:moshi-kotlin-codegen:${Version.moshi}")

  implementation("com.squareup.moshi:moshi:${Version.moshi}")
  implementation("com.squareup.retrofit2:retrofit:${Version.retrofit}")
  implementation("com.squareup.retrofit2:converter-moshi:${Version.retrofit}")

  testImplementation(gradleTestKit())
  testImplementation("junit:junit:${Version.junit}")
  testImplementation("org.assertj:assertj-core:${Version.assertj}")
  testImplementation("com.github.stefanbirkner:system-rules:1.19.0")
  // for non test kit tests
  testImplementation("com.android.tools.build:gradle:${Version.agp}")
  testImplementation(kotlin("gradle-plugin"))
  testImplementation("org.jetbrains.dokka:dokka-gradle-plugin:${Version.dokka}")
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

tasks.test {
  testLogging {
    events("passed", "skipped", "failed")
    setExceptionFormat("full")
  }
}
