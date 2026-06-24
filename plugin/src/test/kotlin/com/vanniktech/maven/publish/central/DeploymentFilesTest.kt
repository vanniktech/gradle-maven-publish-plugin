package com.vanniktech.maven.publish.central

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class DeploymentFilesTest {
  private val allChecksums = setOf("md5", "sha1", "sha256", "sha512")
  private val defaultChecksums = setOf("md5", "sha1")

  @Test
  fun `keeps regular artifacts`() {
    assertThat(
        shouldIncludeInDeployment("lib-1.0.jar", excludeSignatureChecksums = true, defaultChecksums)
      )
      .isTrue()
    assertThat(
        shouldIncludeInDeployment("lib-1.0.pom", excludeSignatureChecksums = true, defaultChecksums)
      )
      .isTrue()
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.module",
          excludeSignatureChecksums = true,
          defaultChecksums,
        )
      )
      .isTrue()
  }

  @Test
  fun `keeps signature files themselves`() {
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.jar.asc",
          excludeSignatureChecksums = true,
          defaultChecksums,
        )
      )
      .isTrue()
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.pom.asc",
          excludeSignatureChecksums = true,
          defaultChecksums,
        )
      )
      .isTrue()
  }

  @Test
  fun `excludes maven-metadata files`() {
    assertThat(
        shouldIncludeInDeployment(
          "maven-metadata.xml",
          excludeSignatureChecksums = true,
          allChecksums,
        )
      )
      .isFalse()
    assertThat(
        shouldIncludeInDeployment(
          "maven-metadata.xml.md5",
          excludeSignatureChecksums = true,
          allChecksums,
        )
      )
      .isFalse()
    assertThat(
        shouldIncludeInDeployment(
          "maven-metadata.xml.sha1",
          excludeSignatureChecksums = true,
          allChecksums,
        )
      )
      .isFalse()
  }

  @Test
  fun `default config keeps md5 and sha1 but drops sha256 and sha512`() {
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.jar.md5",
          excludeSignatureChecksums = true,
          defaultChecksums,
        )
      )
      .isTrue()
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.jar.sha1",
          excludeSignatureChecksums = true,
          defaultChecksums,
        )
      )
      .isTrue()
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.jar.sha256",
          excludeSignatureChecksums = true,
          defaultChecksums,
        )
      )
      .isFalse()
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.jar.sha512",
          excludeSignatureChecksums = true,
          defaultChecksums,
        )
      )
      .isFalse()
  }

  @Test
  fun `allowing all checksums keeps sha256 and sha512`() {
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.jar.sha256",
          excludeSignatureChecksums = true,
          allChecksums,
        )
      )
      .isTrue()
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.jar.sha512",
          excludeSignatureChecksums = true,
          allChecksums,
        )
      )
      .isTrue()
  }

  @Test
  fun `excludes signature checksums when enabled regardless of allowed checksums`() {
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.jar.asc.md5",
          excludeSignatureChecksums = true,
          allChecksums,
        )
      )
      .isFalse()
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.jar.asc.sha1",
          excludeSignatureChecksums = true,
          allChecksums,
        )
      )
      .isFalse()
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.jar.asc.sha256",
          excludeSignatureChecksums = true,
          allChecksums,
        )
      )
      .isFalse()
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.jar.asc.sha512",
          excludeSignatureChecksums = true,
          allChecksums,
        )
      )
      .isFalse()
  }

  @Test
  fun `keeps signature checksums when exclusion disabled and checksum allowed`() {
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.jar.asc.md5",
          excludeSignatureChecksums = false,
          allChecksums,
        )
      )
      .isTrue()
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.jar.asc.sha1",
          excludeSignatureChecksums = false,
          allChecksums,
        )
      )
      .isTrue()
  }

  @Test
  fun `signature checksum exclusion and allowed checksums are independent`() {
    // exclusion disabled but sha256 not allowed -> still dropped because of the checksum allow list
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.jar.asc.sha256",
          excludeSignatureChecksums = false,
          defaultChecksums,
        )
      )
      .isFalse()
    // exclusion disabled and md5 allowed -> kept
    assertThat(
        shouldIncludeInDeployment(
          "lib-1.0.jar.asc.md5",
          excludeSignatureChecksums = false,
          defaultChecksums,
        )
      )
      .isTrue()
  }

  @Test
  fun `checksumExtensionOrNull detects checksum files`() {
    assertThat(checksumExtensionOrNull("lib-1.0.jar.sha256")).isEqualTo("sha256")
    assertThat(checksumExtensionOrNull("lib-1.0.jar.asc.md5")).isEqualTo("md5")
    assertThat(checksumExtensionOrNull("lib-1.0.jar")).isNull()
    assertThat(checksumExtensionOrNull("lib-1.0.jar.asc")).isNull()
  }
}
