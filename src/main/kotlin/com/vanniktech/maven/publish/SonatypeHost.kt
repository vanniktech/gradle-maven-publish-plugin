package com.vanniktech.maven.publish

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
  SO1("https://s01.oss.sonatype.org"),
}
