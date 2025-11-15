package com.vanniktech.maven.publish

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
      return listOf(GradleVersion.VERSIONS.max())
    }
    return GradleVersion.VERSIONS.toList()
  }
}

class AgpVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<AgpVersion> {
    if (QUICK_TEST) {
      return listOf(AgpVersion.VERSIONS.max())
    }
    return AgpVersion.VERSIONS.toList()
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
      return listOf(PluginPublishVersion.VERSIONS.max())
    }
    return PluginPublishVersion.VERSIONS.toList()
  }
}
