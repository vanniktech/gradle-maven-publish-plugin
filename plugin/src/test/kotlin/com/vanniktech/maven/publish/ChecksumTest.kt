package com.vanniktech.maven.publish

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ChecksumTest {
  @Test
  fun `default is md5 and sha1`() {
    assertThat(Checksum.DEFAULT).containsExactly(Checksum.MD5, Checksum.SHA1).inOrder()
  }

  @Test
  fun `extensions match maven layout`() {
    assertThat(Checksum.MD5.extension).isEqualTo("md5")
    assertThat(Checksum.SHA1.extension).isEqualTo("sha1")
    assertThat(Checksum.SHA256.extension).isEqualTo("sha256")
    assertThat(Checksum.SHA512.extension).isEqualTo("sha512")
  }

  @Test
  fun `fromExtension resolves all checksums`() {
    Checksum.entries.forEach { checksum ->
      assertThat(Checksum.fromExtension(checksum.extension)).isEqualTo(checksum)
    }
  }

  @Test
  fun `fromExtension fails for unknown extension`() {
    val exception = assertThrows<IllegalArgumentException> {
      Checksum.fromExtension("sha384")
    }
    assertThat(exception).hasMessageThat().contains("sha384")
  }
}
