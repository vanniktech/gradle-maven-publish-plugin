package com.vanniktech.maven.publish

import java.io.Serializable
import org.gradle.api.provider.Provider

/**
 * Describes the various hosts for Sonatype OSSRH. Depending on when a user signed up with Sonatype
 * they have to use different hosts.
 *
 * https://central.sonatype.org/articles/2021/Feb/23/new-users-on-s01osssonatypeorg/
 */
data class SonatypeHost(
  internal val rootUrl: String,
) : Serializable {
  internal fun apiBaseUrl(): String {
    return "$rootUrl/service/local/"
  }

  internal fun publishingUrl(snapshot: Boolean, stagingRepositoryId: Provider<String>): String {
    return if (snapshot) {
      if (stagingRepositoryId.isPresent) {
        throw IllegalArgumentException("Staging repositories are not supported for SNAPSHOT versions.")
      }
      "$rootUrl/content/repositories/snapshots/"
    } else {
      "$rootUrl/service/local/staging/deployByRepositoryId/${stagingRepositoryId.get()}/"
    }
  }

  companion object {
    @JvmStatic
    fun valueOf(sonatypeHost: String): SonatypeHost = when (sonatypeHost) {
      "DEFAULT" -> DEFAULT
      "S01" -> S01
      else -> throw IllegalArgumentException("No SonatypeHost constant $sonatypeHost")
    }

    @JvmField
    val DEFAULT = SonatypeHost("https://oss.sonatype.org")

    @JvmField
    val S01 = SonatypeHost("https://s01.oss.sonatype.org")
  }
}
