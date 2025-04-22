package com.vanniktech.maven.publish

import java.io.Serializable

/**
 * Describes the various hosts for Sonatype OSSRH. Depending on when a user signed up with Sonatype
 * they have to use different hosts.
 *
 * https://central.sonatype.org/articles/2021/Feb/23/new-users-on-s01osssonatypeorg/
 */
data class SonatypeHost internal constructor(
  internal val rootUrl: String,
  internal val isCentralPortal: Boolean,
) : Serializable {
  constructor(rootUrl: String) : this(rootUrl, isCentralPortal = false)

  internal fun apiBaseUrl(): String {
    return if (isCentralPortal) {
      "$rootUrl/api/v1/"
    } else {
      "$rootUrl/service/local/"
    }
  }

  companion object {
    @JvmStatic
    fun valueOf(sonatypeHost: String): SonatypeHost = when (sonatypeHost) {
      "DEFAULT" -> DEFAULT
      "S01" -> S01
      "CENTRAL_PORTAL" -> CENTRAL_PORTAL
      else -> if (sonatypeHost.startsWith("https://")) {
        SonatypeHost(sonatypeHost)
      } else {
        throw IllegalArgumentException("No SonatypeHost constant $sonatypeHost")
      }
    }

    @JvmField
    val DEFAULT = SonatypeHost("https://oss.sonatype.org", isCentralPortal = false)

    @JvmField
    val S01 = SonatypeHost("https://s01.oss.sonatype.org", isCentralPortal = false)

    @JvmField
    val CENTRAL_PORTAL = SonatypeHost("https://central.sonatype.com", isCentralPortal = true)
  }
}
