package com.vanniktech.maven.publish.util

import com.google.testing.junit.testparameterinjector.junit5.TestParameterValuesProvider
import com.vanniktech.maven.publish.IntegrationTestBuildConfig.QUICK_TEST
import com.vanniktech.maven.publish.IntegrationTestBuildConfig.TEST_CONFIG_METHOD

class TestOptionsConfigProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<TestOptions.Config> = when {
    QUICK_TEST -> listOf(TestOptions.Config.BASE)
    TEST_CONFIG_METHOD.isNotBlank() -> listOf(TestOptions.Config.valueOf(TEST_CONFIG_METHOD))
    else -> TestOptions.Config.entries
  }
}

class GradleVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<GradleVersion> {
    if (QUICK_TEST) {
      return listOf(GradleVersion.entries.max())
    }
    return GradleVersion.entries.toList()
  }
}

class AgpVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<AgpVersion> {
    if (QUICK_TEST) {
      return listOf(AgpVersion.entries.max())
    }
    return AgpVersion.entries.toList()
  }
}

class KgpVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<KgpVersion> {
    if (QUICK_TEST) {
      return listOf(KgpVersion.VERSIONS.max())
    }
    return KgpVersion.VERSIONS.toList()
  }
}

class PluginPublishVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<PluginPublishVersion> {
    if (QUICK_TEST) {
      return listOf(PluginPublishVersion.entries.max())
    }
    return PluginPublishVersion.entries.toList()
  }
}
