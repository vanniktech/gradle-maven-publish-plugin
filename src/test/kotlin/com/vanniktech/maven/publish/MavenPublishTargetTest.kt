package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.MavenPublishPluginExtension.Companion.DEFAULT_TARGET
import com.vanniktech.maven.publish.MavenPublishPluginExtension.Companion.LOCAL_TARGET
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MavenPublishTargetTest {

  @Test
  fun uploadArchivesTaskName() {
    assertThat(MavenPublishTarget(DEFAULT_TARGET).taskName).isEqualTo("uploadArchives")
  }

  @Test
  fun installArchivesTaskName() {
    assertThat(MavenPublishTarget(LOCAL_TARGET).taskName).isEqualTo("installArchives")
  }

  @Test
  fun customTaskName() {
    assertThat(MavenPublishTarget("myRepo").taskName).isEqualTo("uploadArchivesMyRepo")
  }
}
