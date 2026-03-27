package com.vanniktech.maven.publish

/**
 * Wraps a raw `signingInMemoryKey` value and normalizes it to a single-line
 * bare base64 key body.
 *
 * Gradle passes the key string to Bouncy Castle's `PGPUtil.getDecoderStream()`,
 * which can handle both full ASCII-armored and raw base64 formats. The problem
 * is that `gradle.properties` silently truncates multi-line values to the first
 * line, so the full armored format must be collapsed to a single line before it
 * reaches Gradle. Reducing to the bare base64 body (stripping the
 * `-----BEGIN/END-----` headers, blank lines, and armor checksum) is the
 * simplest way to achieve that.
 */
internal class InMemorySigningKey(val rawKey: String) {
  val normalizedKey: String = if (!rawKey.contains("-----BEGIN")) {
    rawKey
  } else {
    val body = rawKey.lines()
      .filter { line ->
        !line.startsWith("-----") && // strip -----BEGIN / -----END headers
          !line.startsWith("=") && // strip OpenPGP armor checksum (e.g. =s83f)
          line.isNotBlank() // strip blank separator lines
      }
      .joinToString("")
    if (body.isEmpty()) {
      throw IllegalArgumentException(
        "signingInMemoryKey looks like a truncated ASCII-armored PGP key. " +
          "This happens when the key is set in gradle.properties, which silently drops every line after the first. " +
          "Use an environment variable instead: ORG_GRADLE_PROJECT_signingInMemoryKey"
      )
    }
    body
  }
}
