package com.vanniktech.maven.publish.nexus

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FindStagingProfileTest {

  @Test
  fun emptyList() {
    val profiles = emptyList<StagingProfile>()
    val exception = assertThrows<IllegalArgumentException> {
      profiles.findStagingProfileForGroup("", "test")
    }

    assertThat(exception)
      .hasMessageThat()
      .isEqualTo("No staging profiles found in account test. Make sure you called \"./gradlew publish\".")
  }

  @Test
  fun oneElement() {
    val profiles = listOf(StagingProfile("1", "profile"))
    val result = profiles.findStagingProfileForGroup("", "")
    assertThat(result).isEqualTo(StagingProfile("1", "profile"))
  }

  @Test
  fun multipleElements() {
    val profiles = listOf(
      StagingProfile("1", "com.foo"),
      StagingProfile("2", "com.bar"),
    )
    val result = profiles.findStagingProfileForGroup("com.foo", "")
    assertThat(result).isEqualTo(StagingProfile("1", "com.foo"))
  }

  @Test
  fun chooseLongestPrefix() {
    val profiles = listOf(
      StagingProfile("1", "com.squareup.misk"),
      StagingProfile("2", "com.squareup"),
    )
    val result = profiles.findStagingProfileForGroup("com.squareup.misk", "")
    assertThat(result).isEqualTo(StagingProfile("1", "com.squareup.misk"))
  }

  @Test
  fun ignoreLongerPrefixThatDoesntMatch() {
    val profiles = listOf(
      StagingProfile("1", "com.squareup.misk"),
      StagingProfile("2", "com.squareup"),
    )
    val result = profiles.findStagingProfileForGroup("com.squareup.okhttp3", "")
    assertThat(result).isEqualTo(StagingProfile("2", "com.squareup"))
  }
}
