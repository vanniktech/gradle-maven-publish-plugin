package com.vanniktech.maven.publish

import org.gradle.api.provider.Provider

/**
 * Describes the various hosts for Sonatype OSSRH. Depending on when a user signed up with Sonatype
 * they have to use different hosts.
 *
 * https://central.sonatype.org/articles/2021/Feb/23/new-users-on-s01osssonatypeorg/
 */
enum class SonatypeHost(
  internal val rootUrl: String
) {
  DEFAULT("https://oss.sonatype.org"),
  S01("https://s01.oss.sonatype.org");

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
      val id = stagingRepositoryId.orNull
      if (id != null) {
        "$rootUrl/service/local/staging/deployByRepositoryId/$id/"
      } else {
        "$rootUrl/service/local/staging/deploy/maven2/"
      }
    }
  }
}
