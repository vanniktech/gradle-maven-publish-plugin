package com.vanniktech.maven.publish.portal

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
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

  @Multipart
  @POST("publisher/upload")
  fun uploadBundle(
    @Query("name") name: String?,
    @Query("publishingType") publishingType: String?,
    @Part input: MultipartBody.Part,
  ): Call<String>
}
