package com.vanniktech.maven.publish

import com.google.common.collect.ImmutableList
import com.google.testing.junit.testparameterinjector.junit5.TestParameterValuesProvider
import com.vanniktech.maven.publish.IntegrationTestBuildConfig.QUICK_TEST
import com.vanniktech.maven.publish.IntegrationTestBuildConfig.TEST_CONFIG_METHOD

internal class TestOptionsConfigProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> = when {
    QUICK_TEST -> ImmutableList.of(TestOptions.Config.BASE)
    TEST_CONFIG_METHOD.isNotBlank() -> ImmutableList.of(TestOptions.Config.valueOf(TEST_CONFIG_METHOD))
    else -> ImmutableList.copyOf(TestOptions.Config.values())
  }
}

internal class GradleVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (QUICK_TEST) {
      return ImmutableList.of(GradleVersion.values().last())
    }
    return ImmutableList.copyOf(GradleVersion.values().distinctBy { it.value })
  }
}

internal class AgpVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (QUICK_TEST) {
      return ImmutableList.of(AgpVersion.values().last())
    }
    return ImmutableList.copyOf(AgpVersion.values().distinctBy { it.value })
  }
}

internal class KotlinVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (QUICK_TEST) {
      return ImmutableList.of(KotlinVersion.values().last())
    }
    return ImmutableList.copyOf(KotlinVersion.values().distinctBy { it.value })
  }
}

internal class GradlePluginPublishVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (QUICK_TEST) {
      return ImmutableList.of(GradlePluginPublish.values().last())
    }
    return ImmutableList.copyOf(GradlePluginPublish.values().distinctBy { it.version })
  }
}
