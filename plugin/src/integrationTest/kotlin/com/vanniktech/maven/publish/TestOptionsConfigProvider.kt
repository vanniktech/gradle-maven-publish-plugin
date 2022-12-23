package com.vanniktech.maven.publish

import com.google.common.collect.ImmutableList
import com.google.testing.junit.testparameterinjector.junit5.TestParameter.TestParameterValuesProvider

internal class TestOptionsConfigProvider : TestParameterValuesProvider {
  override fun provideValues(): ImmutableList<TestOptions.Config?> {
    val property = System.getProperty("testConfigMethod")
    if (property.isNotBlank()) {
      return ImmutableList.of(TestOptions.Config.valueOf(property))
    }
    return ImmutableList.copyOf(TestOptions.Config.values())
  }
}
