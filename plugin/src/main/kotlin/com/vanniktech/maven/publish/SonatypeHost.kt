package com.vanniktech.maven.publish

import java.io.Serializable

/**
 * Describes the various hosts for Sonatype OSSRH. Depending on when a user signed up with Sonatype
 * they have to use different hosts.
 *
 * https://central.sonatype.org/articles/2021/Feb/23/new-users-on-s01osssonatypeorg/
 */
public data class SonatypeHost internal constructor(
  internal val rootUrl: String,
  internal val isCentralPortal: Boolean,
) : Serializable {
  public constructor(rootUrl: String) : this(rootUrl, isCentralPortal = false)

  internal fun apiBaseUrl(): String {
    return if (isCentralPortal) {
      "$rootUrl/api/v1/"
    } else {
      "$rootUrl/service/local/"
    }
  }

  public companion object {
    @JvmStatic
    public fun valueOf(sonatypeHost: String): SonatypeHost = when (sonatypeHost) {
      "DEFAULT" -> DEFAULT
      "S01" -> S01
      "CENTRAL_PORTAL" -> CENTRAL_PORTAL
      else -> if (sonatypeHost.startsWith("https://")) {
        SonatypeHost(sonatypeHost)
      } else {
        throw IllegalArgumentException("No SonatypeHost constant $sonatypeHost")
      }
    }

    @Deprecated(
      message = "OSSRH is deprecated, migrate to CENTRAL_PORTAL instead. " +
        "See more info at https://central.sonatype.org/news/20250326_ossrh_sunset.",
    )
    @JvmField
    public val DEFAULT: SonatypeHost = SonatypeHost("https://oss.sonatype.org", isCentralPortal = false)

    @Deprecated(
      message = "OSSRH is deprecated, migrate to CENTRAL_PORTAL instead. " +
        "See more info at https://central.sonatype.org/news/20250326_ossrh_sunset.",
    )
    @JvmField
    public val S01: SonatypeHost = SonatypeHost("https://s01.oss.sonatype.org", isCentralPortal = false)

    @JvmField
    public val CENTRAL_PORTAL: SonatypeHost = SonatypeHost("https://central.sonatype.com", isCentralPortal = true)
  }
}
