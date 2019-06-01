package com.vanniktech.maven.publish.nexus.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Repository(val repositoryId: String, val transitioning: Boolean, val type: String)

@JsonClass(generateAdapter = true)
data class ProfileRepositoriesResponse(val data: List<Repository>)

@JsonClass(generateAdapter = true)
data class TransitionRepositoryInputData(val stagedRepositoryIds: List<String>, val autoDropAfterRelease: Boolean? = null)

@JsonClass(generateAdapter = true)
data class TransitionRepositoryInput(val data: TransitionRepositoryInputData)
