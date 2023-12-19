package com.vanniktech.maven.publish.nexus

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/* Nexus service definition based on the incomplete documentation at:
 * -Nexus STAGING API: https://oss.sonatype.org/nexus-staging-plugin/default/docs/index.html
 * -Nexus CORE API: https://repository.sonatype.org/nexus-restlet1x-plugin/default/docs/index.html
 */
internal interface NexusService {
  @GET("staging/profiles")
  fun getStagingProfiles(): Call<StagingProfilesResponse>

  @POST("staging/profiles/{profileId}/start")
  fun createRepository(
    @Path("profileId") stagingProfileId: String,
    @Body input: CreateRepositoryInput,
  ): Call<CreateRepositoryResponse>

  @GET("staging/profile_repositories")
  fun getProfileRepositories(): Call<ProfileRepositoriesResponse>

  @GET("staging/repository/{repositoryId}")
  fun getRepository(
    @Path("repositoryId") repositoryId: String,
  ): Call<Repository>

  @GET("staging/repository/{repositoryId}/activity")
  fun getRepositoryActivity(
    @Path("repositoryId") repositoryId: String,
  ): Call<List<RepositoryActivity>>

  @POST("staging/bulk/close")
  fun closeRepository(
    @Body input: TransitionRepositoryInput,
  ): Call<Unit>

  @POST("staging/bulk/promote")
  fun releaseRepository(
    @Body input: TransitionRepositoryInput,
  ): Call<Unit>

  @POST("staging/bulk/drop")
  fun dropRepository(
    @Body input: TransitionRepositoryInput,
  ): Call<Unit>
}
