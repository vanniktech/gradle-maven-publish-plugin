package com.vanniktech.maven.publish

import com.vanniktech.maven.publish.sonatype.SonatypeRepositoryBuildService
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

  internal fun publishingUrl(
    snapshot: Provider<Boolean>,
    buildService: Provider<SonatypeRepositoryBuildService>,
    configCache: Boolean,
  ): String {
    return if (snapshot.get()) {
      require(buildService.get().stagingRepositoryId == null) {
        "Staging repositories are not supported for SNAPSHOT versions."
      }
      "$rootUrl/content/repositories/snapshots/"
    } else {
      val stagingRepositoryId = buildService.get().stagingRepositoryId
      requireNotNull(stagingRepositoryId) {
        if (configCache) {
          "Publishing releases to Maven Central is not supported yet with configuration caching enabled, because of " +
            "this missing Gradle feature: https://github.com/gradle/gradle/issues/22779"
        } else {
          "The staging repository was not created yet. Please open a bug with a build scan or build logs and stacktrace"
        }
      }

      "$rootUrl/service/local/staging/deployByRepositoryId/$stagingRepositoryId/"
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
