package com.vanniktech.maven.publish.util

import com.google.testing.junit.testparameterinjector.junit5.TestParameterValuesProvider
import com.vanniktech.maven.publish.IntegrationTestBuildConfig.QUICK_TEST
import com.vanniktech.maven.publish.IntegrationTestBuildConfig.TEST_CONFIG_METHOD

class TestOptionsConfigProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<TestOptions.Config> = when {
    TEST_CONFIG_METHOD.isNotBlank() -> listOf(TestOptions.Config.valueOf(TEST_CONFIG_METHOD))
    QUICK_TEST -> listOf(TestOptions.Config.BASE)
    else -> TestOptions.Config.entries
  }
}

class GradleVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<GradleVersion> = when {
    QUICK_TEST -> listOf(GradleVersion.VERSIONS.max())
    else -> GradleVersion.VERSIONS.toList()
  }
}

class AgpVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<AgpVersion> = when {
    QUICK_TEST -> listOf(AgpVersion.VERSIONS.max())
    else -> AgpVersion.VERSIONS.toList()
  }
}

class KgpVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<KgpVersion> = when {
    QUICK_TEST -> listOf(KgpVersion.VERSIONS.max())
    else -> KgpVersion.VERSIONS.toList()
  }
}

class PluginPublishVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<PluginPublishVersion> = when {
    QUICK_TEST -> listOf(PluginPublishVersion.VERSIONS.max())
    else -> PluginPublishVersion.VERSIONS.toList()
  }
}
