plugins {
  id("shared")
  id("java-gradle-plugin")
}

gradlePlugin {
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

val integrationTestSourceSet = sourceSets.create("integrationTest") {
  compileClasspath += sourceSets["main"].output + configurations.testRuntimeClasspath
  runtimeClasspath += output + compileClasspath
}
val integrationTestImplementation = configurations.getByName("integrationTestImplementation")
  .extendsFrom(configurations.testImplementation.get())

dependencies {
  api(gradleApi())
  api(libs.kotlin.stdlib)

  compileOnly(libs.dokka)
  compileOnly(libs.kotlin.plugin)
  compileOnly(libs.android.plugin)

  implementation(projects.nexus)

  testImplementation(gradleTestKit())
  testImplementation(libs.junit)
  testImplementation(libs.assertj)
}

val integrationTest by tasks.registering(Test::class) {
  dependsOn(
    tasks.publishToMavenLocal,
    projects.nexus.dependencyProject.tasks.publishToMavenLocal
  )
  mustRunAfter(tasks.test)

  description = "Runs the integration tests."
  group = "verification"

  testClassesDirs = integrationTestSourceSet.output.classesDirs
  classpath = integrationTestSourceSet.runtimeClasspath

  testLogging.showStandardStreams = true

  systemProperty("com.vanniktech.publish.version", version.toString())

  beforeTest(
    closureOf<TestDescriptor> {
      logger.lifecycle("Running test: $this")
    }
  )
}

tasks.check {
  dependsOn(integrationTest)
}
