package com.vanniktech.maven.publish

import com.google.common.collect.ImmutableList
import com.google.testing.junit.testparameterinjector.junit5.TestParameterValuesProvider

private val quickTestProperty get() = System.getProperty("quickTest")

internal class TestOptionsConfigProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    val property = System.getProperty("testConfigMethod")
    if (property.isNotBlank()) {
      return ImmutableList.of(TestOptions.Config.valueOf(property))
    }
    if (quickTestProperty.isNotBlank()) {
      return ImmutableList.of(TestOptions.Config.BASE)
    }
    return ImmutableList.copyOf(TestOptions.Config.values())
  }
}

internal class GradleVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (quickTestProperty.isNotBlank()) {
      return ImmutableList.of(GradleVersion.values().last())
    }
    return ImmutableList.copyOf(GradleVersion.values().distinctBy { it.value })
  }
}

internal class AgpVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (quickTestProperty.isNotBlank()) {
      return ImmutableList.of(AgpVersion.values().last())
    }
    return ImmutableList.copyOf(AgpVersion.values().distinctBy { it.value })
  }
}

internal class KotlinVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (quickTestProperty.isNotBlank()) {
      return ImmutableList.of(KotlinVersion.values().last())
    }
    return ImmutableList.copyOf(KotlinVersion.values().distinctBy { it.value })
  }
}

internal class GradlePluginPublishVersionProvider : TestParameterValuesProvider() {
  override fun provideValues(context: Context?): List<*> {
    if (quickTestProperty.isNotBlank()) {
      return ImmutableList.of(GradlePluginPublish.entries.last())
    }
    return ImmutableList.copyOf(GradlePluginPublish.values().distinctBy { it.version })
  }
}
