package com.vanniktech.maven.publish

import com.google.testing.junit.testparameterinjector.junit5.TestParameterValuesProvider
import com.vanniktech.maven.publish.IntegrationTestBuildConfig.QUICK_TEST
import com.vanniktech.maven.publish.IntegrationTestBuildConfig.TEST_CONFIG_METHOD

class TestOptionsConfigProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> = when {
    QUICK_TEST -> listOf(TestOptions.Config.BASE)
    TEST_CONFIG_METHOD.isNotBlank() -> listOf(TestOptions.Config.valueOf(TEST_CONFIG_METHOD))
    else -> TestOptions.Config.entries
  }
}

class GradleVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (QUICK_TEST) {
      return listOf(GradleVersion.VERSIONS.max())
    }
    return GradleVersion.VERSIONS.toList()
  }
}

class AgpVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (QUICK_TEST) {
      return listOf(AgpVersion.VERSIONS.max())
    }
    return AgpVersion.VERSIONS.toList()
  }
}

class KotlinVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (QUICK_TEST) {
      return listOf(KgpVersion.VERSIONS.max())
    }
    return KgpVersion.VERSIONS.toList()
  }
}

class GradlePluginPublishVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (QUICK_TEST) {
      return listOf(GradlePluginPublish.VERSIONS.max())
    }
    return GradlePluginPublish.VERSIONS.toList()
  }
}
