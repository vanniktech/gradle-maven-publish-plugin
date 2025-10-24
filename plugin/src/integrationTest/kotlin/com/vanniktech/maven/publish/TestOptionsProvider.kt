package com.vanniktech.maven.publish

import com.google.testing.junit.testparameterinjector.junit5.TestParameterValuesProvider
import com.vanniktech.maven.publish.IntegrationTestBuildConfig.QUICK_TEST
import com.vanniktech.maven.publish.IntegrationTestBuildConfig.TEST_CONFIG_METHOD

internal class TestOptionsConfigProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> = when {
    QUICK_TEST -> listOf(TestOptions.Config.BASE)
    TEST_CONFIG_METHOD.isNotBlank() -> listOf(TestOptions.Config.valueOf(TEST_CONFIG_METHOD))
    else -> TestOptions.Config.values().toList()
  }
}

internal class GradleVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (QUICK_TEST) {
      return listOf(GradleVersion.values().last())
    }
    return GradleVersion.values().distinctBy { it.value }
  }
}

internal class AgpVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (QUICK_TEST) {
      return listOf(AgpVersion.values().last())
    }
    return AgpVersion.values().distinctBy { it.value }
  }
}

internal class KotlinVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (QUICK_TEST) {
      return listOf(KotlinVersion.values().last())
    }
    return KotlinVersion.values().distinctBy { it.value }
  }
}

internal class GradlePluginPublishVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (QUICK_TEST) {
      return listOf(GradlePluginPublish.values().last())
    }
    return GradlePluginPublish.values().distinctBy { it.version }
  }
}
