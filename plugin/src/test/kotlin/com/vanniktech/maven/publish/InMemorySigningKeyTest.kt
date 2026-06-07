package com.vanniktech.maven.publish

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Properties

class InMemorySigningKeyTest {

  // The stripped key body used in integration tests (single-line bare base64, safe for gradle.properties).
  private val strippedKey =
    "lQdGBF4jUfwBEACblZV4uBViHcYLOb2280tEpr64iB9b6YRkWil3EODiiLd9JS3V9pip+B1QLw" +
      "EdLCEJA+3IIiw4qM5hnMw=="

  // The same key body wrapped in full ASCII-armor format, exactly as
  // `gpg --export-secret-keys --armor` produces it.
  private val fullArmoredKey = """
    -----BEGIN PGP PRIVATE KEY BLOCK-----

    lQdGBF4jUfwBEACblZV4uBViHcYLOb2280tEpr64iB9b6YRkWil3EODiiLd9JS3V9
    pip+B1QLwEdLCEJA+3IIiw4qM5hnMw==
    =s83f
    -----END PGP PRIVATE KEY BLOCK-----
  """.trimIndent()

  @Test
  fun `key without headers is returned unchanged`() {
    val key = InMemorySigningKey(strippedKey)
    assertThat(key.normalizedKey).isEqualTo(strippedKey)
  }

  @Test
  fun `key with PGP armor headers is stripped to bare base64`() {
    val key = InMemorySigningKey(fullArmoredKey)
    assertThat(key.normalizedKey).doesNotContain("-----BEGIN")
    assertThat(key.normalizedKey).doesNotContain("-----END")
    assertThat(key.normalizedKey).doesNotContain("\n")
  }

  @Test
  fun `armor checksum line is removed`() {
    val key = InMemorySigningKey(fullArmoredKey)
    // The checksum line starts with '=' and is stripped as part of collapsing to a single line
    assertThat(key.normalizedKey).doesNotContain("=s83f")
    // Ensure the trailing '==' from base64 padding is still present
    assertThat(key.normalizedKey).endsWith("==")
  }

  @Test
  fun `full armored key and stripped key produce the same normalized result`() {
    val fromArmored = InMemorySigningKey(fullArmoredKey).normalizedKey
    val fromStripped = InMemorySigningKey(strippedKey).normalizedKey
    assertThat(fromArmored).isEqualTo(fromStripped)
  }

  @Test
  fun `gradle properties truncates armored key to first line making normalization ineffective`() {
    // Prove why gradle.properties cannot be used with full ASCII-armored keys.
    // java.util.Properties (which Gradle uses) treats each newline as a new entry,
    // so only the first line of a multi-line value is ever read.
    val propertiesContent = "signingInMemoryKey=$fullArmoredKey"
    val props = Properties().apply { load(propertiesContent.reader()) }
    val valueReadByGradle = props.getProperty("signingInMemoryKey")

    // Gradle only sees the first line of the armored block.
    assertThat(valueReadByGradle).isEqualTo("-----BEGIN PGP PRIVATE KEY BLOCK-----")

    // Normalizing this truncated value throws a clear error pointing the user to env vars.
    val ex = assertThrows<IllegalArgumentException> { InMemorySigningKey(valueReadByGradle).normalizedKey }
    assertThat(ex).hasMessageThat().contains("ORG_GRADLE_PROJECT_signingInMemoryKey")
  }
}
