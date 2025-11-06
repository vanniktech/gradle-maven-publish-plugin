import org.gradle.api.plugins.JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME

plugins {
  id("shared")
  id("java-gradle-plugin")
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.android.lint)
}

gradlePlugin {
  plugins {
    create("mavenPublishPlugin") {
      id = "com.vanniktech.maven.publish"
      implementationClass = "com.vanniktech.maven.publish.MavenPublishPlugin"
      displayName = "Gradle Maven Publish Plugin"
      description = "Gradle plugin that configures publish tasks to automatically upload all of your Java, Kotlin, " +
        "Gradle, or Android libraries to any Maven instance."
    }
    create("mavenPublishBasePlugin") {
      id = "com.vanniktech.maven.publish.base"
      implementationClass = "com.vanniktech.maven.publish.MavenPublishBasePlugin"
      displayName = "Gradle Maven Publish Base Plugin"
      description = "Gradle plugin that configures publish tasks to automatically upload all of your Java, Kotlin, " +
        "Gradle, or Android libraries to any Maven instance."
    }
  }
}

val integrationTestSourceSet = sourceSets.create("integrationTest") {
  compileClasspath += sourceSets["main"].output
  compileClasspath += configurations.testRuntimeClasspath.get()
  runtimeClasspath += output + compileClasspath
}
val integrationTestImplementation = configurations["integrationTestImplementation"]
  .extendsFrom(configurations.testImplementation.get())

configurations.named(API_ELEMENTS_CONFIGURATION_NAME) {
  attributes.attribute(
    // TODO: https://github.com/gradle/gradle/issues/24608
    GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
    objects.named(libs.versions.minGradle.get()),
  )
}

buildConfig {
  packageName("com.vanniktech.maven.publish")
  generateAtSync = true

  sourceSets.named("main") {
    buildConfigField("PLUGIN_NAME", "com.vanniktech.maven.publish")
    buildConfigField("VERSION_NAME", providers.gradleProperty("VERSION_NAME"))
  }

  sourceSets.named(integrationTestSourceSet.name) {
    // We must provide the plugin version here instead of using `withPluginClasspath` for GradleRunner. As there are
    // various AGP / KGP and other plugins tested in the matrix, `withPluginClasspath` will mess the whole classpath.
    buildConfigField("VERSION_NAME", providers.gradleProperty("VERSION_NAME"))
    buildConfigField("TEST_CONFIG_METHOD", providers.gradleProperty("testConfigMethod").orElse(""))
    buildConfigField("Boolean", "QUICK_TEST", providers.gradleProperty("quickTest").orElse("false"))

    buildConfigField("GRADLE_MIN", libs.versions.minGradle)
    buildConfigField("GRADLE_ALPHA", alpha.versions.gradle.asProvider())
    buildConfigField("GRADLE_BETA", beta.versions.gradle.asProvider())
    buildConfigField("GRADLE_RC", rc.versions.gradle.asProvider())
    buildConfigField("GRADLE_STABLE", libs.versions.gradle.asProvider())
    buildConfigField("ANDROID_GRADLE_ALPHA", alpha.versions.android.gradle)
    buildConfigField("ANDROID_GRADLE_BETA", beta.versions.android.gradle)
    buildConfigField("ANDROID_GRADLE_RC", rc.versions.android.gradle)
    buildConfigField("ANDROID_GRADLE_STABLE", libs.versions.android.gradle)
    buildConfigField("KOTLIN_ALPHA", alpha.versions.kotlin)
    buildConfigField("KOTLIN_BETA", beta.versions.kotlin)
    buildConfigField("KOTLIN_RC", rc.versions.kotlin)
    buildConfigField("KOTLIN_STABLE", libs.versions.kotlin)
    buildConfigField("GRADLE_PUBLISH_ALPHA", alpha.versions.gradle.plugin.publish)
    buildConfigField("GRADLE_PUBLISH_BETA", beta.versions.gradle.plugin.publish)
    buildConfigField("GRADLE_PUBLISH_RC", rc.versions.gradle.plugin.publish)
    buildConfigField("GRADLE_PUBLISH_STABLE", libs.versions.gradle.plugin.publish)
  }
}

lint {
  baseline = file("lint-baseline.xml")
  ignoreTestSources = true
  warningsAsErrors = true
  disable += "NewerVersionAvailable"
  disable += "GradleDependency"
  disable += "AndroidGradlePluginVersion"
}

dependencies {
  compileOnly(libs.dokka)
  compileOnly(libs.kotlin.plugin)
  compileOnly(libs.android.plugin)

  implementation(projects.centralPortal)

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.engine)
  testImplementation(libs.junit.launcher)
  testImplementation(libs.testParameterInjector)
  testImplementation(libs.truth)
  testImplementation(libs.truth.testKit)
  testImplementation(libs.maven.model)

  lintChecks(libs.androidx.gradlePluginLints)
}

tasks.validatePlugins {
  // TODO: https://github.com/gradle/gradle/issues/22600
  enableStricterValidation = true
}

tasks.whenTaskAdded {
  if (name.contains("lint") && this::class.java.name.contains("com.android.build")) {
    // TODO: lints can be run on Java 17 or above, remove this once we bump the min Java version to 17.
    enabled = JavaVersion.current() >= JavaVersion.VERSION_17
  }
}

val integrationTest by tasks.registering(Test::class) {
  dependsOn(
    tasks.publishToMavenLocal,
    project(projects.centralPortal.path).tasks.publishToMavenLocal,
  )
  mustRunAfter(tasks.test)

  description = "Runs the integration tests."
  group = "verification"

  testClassesDirs = integrationTestSourceSet.output.classesDirs
  classpath = integrationTestSourceSet.runtimeClasspath

  val testJavaVersion = System.getProperty("testJavaVersion")
  if (testJavaVersion != null) {
    javaLauncher = javaToolchains.launcherFor {
      languageVersion = JavaLanguageVersion.of(testJavaVersion.toInt())
    }
  }

  useJUnitPlatform()
  testLogging.showStandardStreams = true
  maxHeapSize = "2g"
  maxParallelForks = Runtime.getRuntime().availableProcessors()
  jvmArgs(
    "--add-opens",
    "java.base/java.lang.invoke=ALL-UNNAMED",
    "--add-opens",
    "java.base/java.net=ALL-UNNAMED",
    "--add-opens",
    "java.base/java.util=ALL-UNNAMED",
  )

  beforeTest(
    closureOf<TestDescriptor> {
      logger.lifecycle("Running test: ${this.className} ${this.displayName}")
    },
  )
}

tasks.check {
  dependsOn(integrationTest)
}
