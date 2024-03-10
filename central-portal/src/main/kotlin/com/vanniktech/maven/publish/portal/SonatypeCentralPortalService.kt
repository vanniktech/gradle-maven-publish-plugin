package com.vanniktech.maven.publish.portal

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/*
* Sonatype Central Portal Publishing based on https://central.sonatype.org/publish/publish-portal-api/
 */
internal interface SonatypeCentralPortalService {
  @DELETE("publisher/deployment/{deploymentId}")
  fun deleteDeployment(
    @Path("deploymentId") deploymentId: String,
  ): Call<Unit>

  @POST("publisher/deployment/{deploymentId}")
  fun publishDeployment(
    @Path("deploymentId") deploymentId: String,
  ): Call<Unit>

  @Streaming
  @GET("publisher/deployment/download/{deploymentId}/{relativePath}")
  suspend fun getDeploymentDownloadByIdAndPath(
    @Path("deploymentId") deploymentId: String,
    @Path("relativePath") relativePath: String,
  ): Call<ResponseBody>

  @GET("publisher/published")
  fun getPublished(
    @Query("namespace") namespace: String,
    @Query("name") name: String,
    @Query("version") version: String,
  ): Call<String>

  @POST("publisher/status")
  fun getStatus(
    @Query("id") id: String,
  ): Call<DeploymentStatus>

  @Multipart
  @POST("publisher/upload")
  fun uploadBundle(
    @Query("name") name: String?,
    @Query("publishingType") publishingType: String?,
    @Part input: MultipartBody.Part,
  ): Call<String>

  @Streaming
  @GET("publisher/deployments/download")
  suspend fun getDeploymentDownload(
    @Query("relativePath") relativePath: String,
  ): Call<ResponseBody>
}
