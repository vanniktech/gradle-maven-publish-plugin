package com.vanniktech.maven.publish.central

import com.vanniktech.maven.publish.Checksum

private val CHECKSUM_EXTENSIONS = Checksum.entries.map { it.extension }

// The checksum extension of a file (e.g. "sha256" for "foo.jar.sha256"), or null if it is not a
// checksum file.
internal fun checksumExtensionOrNull(fileName: String): String? = CHECKSUM_EXTENSIONS.firstOrNull {
  fileName.endsWith(".$it")
}

/**
 * Decides whether a file from the local repository should be included in the Maven Central
 * deployment.
 *
 * @param fileName the name of the file
 * @param excludeSignatureChecksums whether checksums of signature (`.asc`) files should be
 *   excluded, see [gradle/gradle#20232](https://github.com/gradle/gradle/issues/20232)
 * @param allowedChecksumExtensions the checksum extensions (e.g. `md5`, `sha1`) that should be
 *   published
 */
internal fun shouldIncludeInDeployment(
  fileName: String,
  excludeSignatureChecksums: Boolean,
  allowedChecksumExtensions: Set<String>,
): Boolean {
  // maven-metadata files are not needed by Maven Central.
  if (fileName.contains("maven-metadata")) {
    return false
  }

  val checksumExtension = checksumExtensionOrNull(fileName) ?: return true

  if (excludeSignatureChecksums && fileName.removeSuffix(".$checksumExtension").endsWith(".asc")) {
    return false
  }

  return checksumExtension in allowedChecksumExtensions
}
