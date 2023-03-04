package com.vanniktech.maven.publish.nexus

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class StagingProfile(val id: String, val name: String)

@JsonClass(generateAdapter = true)
internal data class StagingProfilesResponse(val data: List<StagingProfile>)

@JsonClass(generateAdapter = true)
internal data class CreateRepositoryInputData(val description: String)

@JsonClass(generateAdapter = true)
internal data class CreateRepositoryInput(val data: CreateRepositoryInputData)

@JsonClass(generateAdapter = true)
internal data class CreatedRepository(val stagedRepositoryId: String)

@JsonClass(generateAdapter = true)
internal data class CreateRepositoryResponse(val data: CreatedRepository)

@JsonClass(generateAdapter = true)
internal data class Repository(
  val repositoryId: String,
  val transitioning: Boolean,
  val type: String,
  val notifications: Int,
)

@JsonClass(generateAdapter = true)
internal data class RepositoryEventProperty(val name: String, val value: String)

@JsonClass(generateAdapter = true)
internal data class RepositoryEvent(val name: String, val properties: List<RepositoryEventProperty>)

@JsonClass(generateAdapter = true)
internal data class RepositoryActivity(val name: String, val events: List<RepositoryEvent>)

@JsonClass(generateAdapter = true)
internal data class ProfileRepositoriesResponse(val data: List<Repository>)

@JsonClass(generateAdapter = true)
internal data class TransitionRepositoryInputData(val stagedRepositoryIds: List<String>, val autoDropAfterRelease: Boolean? = null)

@JsonClass(generateAdapter = true)
internal data class TransitionRepositoryInput(val data: TransitionRepositoryInputData)
