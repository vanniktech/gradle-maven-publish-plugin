package com.vanniktech.maven.publish.nexus

internal fun List<StagingProfile>.findStagingProfileForGroup(group: String, username: String): StagingProfile {
  val allProfiles = this

  if (allProfiles.isEmpty()) {
    throw IllegalArgumentException("No staging profiles found in account $username. Make sure you called \"./gradlew publish\".")
  }

  if (allProfiles.size == 1) {
    return allProfiles[0]
  }

  val exactMatch = allProfiles.find { group == it.name }
  if (exactMatch != null) {
    return exactMatch
  }

  val longestPrefixMatch = allProfiles
    .filter { group.startsWith(it.name) }
    .maxByOrNull { group.commonPrefixWith(it.name).length }
  if (longestPrefixMatch != null) {
    return longestPrefixMatch
  }

  throw IllegalArgumentException(
    "No matching staging profile found in account $username. It is expected that the account contains a staging " +
      "profile that matches or is the start of $group. " +
      "Available profiles are: ${allProfiles.joinToString(separator = ", ") { it.name }}",
  )
}
