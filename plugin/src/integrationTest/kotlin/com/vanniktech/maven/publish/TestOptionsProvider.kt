package com.vanniktech.maven.publish

import com.google.testing.junit.testparameterinjector.junit5.TestParameterValuesProvider
import com.vanniktech.maven.publish.IntegrationTestBuildConfig.QUICK_TEST
import com.vanniktech.maven.publish.IntegrationTestBuildConfig.TEST_CONFIG_METHOD

internal class TestOptionsConfigProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> = when {
    QUICK_TEST -> listOf(TestOptions.Config.BASE)
    TEST_CONFIG_METHOD.isNotBlank() -> listOf(TestOptions.Config.valueOf(TEST_CONFIG_METHOD))
    else -> TestOptions.Config.entries
  }
}

internal class GradleVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (QUICK_TEST) {
      return listOf(GradleVersion.VERSIONS.max())
    }
    return GradleVersion.VERSIONS.toList()
  }
}

internal class AgpVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (QUICK_TEST) {
      return listOf(AgpVersion.VERSIONS.max())
    }
    return AgpVersion.VERSIONS.toList()
  }
}

internal class KotlinVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (QUICK_TEST) {
      return listOf(KotlinVersion.VERSIONS.max())
    }
    return KotlinVersion.VERSIONS.toList()
  }
}

internal class GradlePluginPublishVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (QUICK_TEST) {
      return listOf(GradlePluginPublish.VERSIONS.max())
    }
    return GradlePluginPublish.VERSIONS.toList()
  }
}
