plugins {
  id("shared")
  id("java-gradle-plugin")
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

val integrationTestSourceSet = sourceSets.create("integrationTest") {
  compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
  runtimeClasspath += output + compileClasspath
}
val integrationTestImplementation = configurations.getByName("integrationTestImplementation")
  .extendsFrom(configurations.getByName("testImplementation"))

dependencies {
  api(gradleApi())
  api(kotlin("stdlib"))

  compileOnly("org.jetbrains.dokka:dokka-gradle-plugin:${Version.dokka}")
  compileOnly(kotlin("gradle-plugin"))
  compileOnly("com.android.tools.build:gradle:${Version.agp}")

  implementation(project(":nexus"))

  testImplementation(gradleTestKit())
  testImplementation("junit:junit:${Version.junit}")
  testImplementation("org.assertj:assertj-core:${Version.assertj}")
}

val integrationTest by tasks.registering(Test::class) {
  dependsOn("publishToMavenLocal", project(":nexus").tasks.named("publishToMavenLocal"))
  mustRunAfter(tasks.named("test"))

  description = "Runs the integration tests."
  group = "verification"

  testClassesDirs = integrationTestSourceSet.output.classesDirs
  classpath = integrationTestSourceSet.runtimeClasspath

  systemProperty("com.vanniktech.publish.version", version.toString())

  beforeTest(
    closureOf<TestDescriptor> {
      logger.lifecycle("Running test: $this")
    }
  )
}

val check = tasks.named("check")
check.configure {
  dependsOn(integrationTest)
}
