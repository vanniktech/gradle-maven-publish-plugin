package com.vanniktech.maven.publish.portal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class FileRequest(
  val page: Int,
  @Json(name = "size") val sizze: Int,
  val sortField: String,
  val sortDirection: String,
  val deploymentIds: List<String>,
  val pathStarting: String,
)

@JsonClass(generateAdapter = true)
internal data class DeployedComponentVersion(
  val name: String,
  val path: String,
  val errors: List<String>,
)

@JsonClass(generateAdapter = true)
internal data class DeploymentResponseFile(
  val deploymentId: String,
  val deploymentName: String,
  val deploymentState: String,
  val deploymentType: String,
  val createTimestamp: Long,
  val purls: List<String>,
  val deployedComponentVersions: List<DeployedComponentVersion>,
)

@JsonClass(generateAdapter = true)
internal data class DeploymentStatus(
  val deploymentId: String,
  val deploymentName: String,
  val deploymentState: String,
  val purls: List<String>,
  val errors: List<String>,
)

@JsonClass(generateAdapter = true)
internal data class DeploymentResponseFiles(
  val deployments: List<DeploymentResponseFile>,
  val page: Int,
  val pageSize: Int,
  val pageCount: Int,
  val totalResultCount: Int,
)
