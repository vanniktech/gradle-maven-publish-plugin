package com.vanniktech.maven.publish

import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.vanniktech.maven.publish.TestOptions.Signing.IN_MEMORY_KEY
import java.nio.file.Path
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

  @BeforeEach
  fun setup() {
    gradleVersion.assumeSupportedJdkVersion()
  }
}
