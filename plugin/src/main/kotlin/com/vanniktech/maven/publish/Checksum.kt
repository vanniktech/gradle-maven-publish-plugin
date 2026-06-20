package com.vanniktech.maven.publish

/**
 * Checksum types that Gradle generates for published files. [SHA256] and [SHA512] are not read by Gradle or
 * Maven Central and are therefore not published by default.
 */
public enum class Checksum(
  internal val extension: String,
) {
  MD5("md5"),
  SHA1("sha1"),
  SHA256("sha256"),
  SHA512("sha512"),
  ;

  internal companion object {
    val DEFAULT: List<Checksum> = listOf(MD5, SHA1)

    fun fromExtension(extension: String): Checksum = entries.firstOrNull { it.extension == extension }
      ?: throw IllegalArgumentException(
        "Unknown checksum \"$extension\". Valid values are: ${entries.joinToString { it.extension }}.",
      )
  }
}
