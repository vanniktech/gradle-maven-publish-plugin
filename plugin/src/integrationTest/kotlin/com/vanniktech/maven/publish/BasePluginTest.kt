package com.vanniktech.maven.publish

import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.vanniktech.maven.publish.util.GradleVersion
import com.vanniktech.maven.publish.util.GradleVersionProvider
import com.vanniktech.maven.publish.util.ProjectSpec
import com.vanniktech.maven.publish.util.TestOptions
import com.vanniktech.maven.publish.util.TestOptions.Signing.IN_MEMORY_KEY
import com.vanniktech.maven.publish.util.TestOptionsConfigProvider
import com.vanniktech.maven.publish.util.assumeSupportedJdkVersion
import com.vanniktech.maven.publish.util.run
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir

abstract class BasePluginTest {
  @TempDir
  lateinit var testProjectDir: Path
    private set

  @TestParameter(valuesProvider = TestOptionsConfigProvider::class)
  lateinit var config: TestOptions.Config
    private set

  @TestParameter(valuesProvider = GradleVersionProvider::class)
  lateinit var gradleVersion: GradleVersion
    private set

  open val testOptions get() = TestOptions(config, IN_MEMORY_KEY, gradleVersion)

  fun ProjectSpec.run(options: TestOptions = testOptions) = run(fixtures, testProjectDir, options)

  @BeforeEach
  fun setup() {
    gradleVersion.assumeSupportedJdkVersion()
  }
}

private val fixtures = Path("src/integrationTest/fixtures2").absolute()
