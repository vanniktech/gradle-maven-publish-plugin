package com.vanniktech.maven.publish.nexus

import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class Nexus(
  baseUrl: String,
  private val username: String,
  password: String,
) {
  private val service by lazy {
    val okHttpClient = OkHttpClient.Builder()
      .addInterceptor(NexusOkHttpInterceptor(username, password))
      .connectTimeout(60, TimeUnit.SECONDS)
      .readTimeout(60, TimeUnit.SECONDS)
      .writeTimeout(60, TimeUnit.SECONDS)
      .build()
    val retrofit = Retrofit.Builder()
      .addConverterFactory(MoshiConverterFactory.create())
      .client(okHttpClient)
      .baseUrl(baseUrl)
      .build()

    retrofit.create(NexusService::class.java)
  }

  private fun getProfiles(): List<StagingProfile>? {
    val stagingProfilesResponse = service.getStagingProfiles().execute()

    if (!stagingProfilesResponse.isSuccessful) {
      throw IOException("Cannot get stagingProfiles for account $username: ${stagingProfilesResponse.errorBody()?.string()}")
    }

    return stagingProfilesResponse.body()?.data
  }

  private fun findStagingProfile(group: String): StagingProfile {
    val allProfiles = getProfiles() ?: emptyList()

    if (allProfiles.isEmpty()) {
      throw IllegalArgumentException("No staging profiles found in account $username. Make sure you called \"./gradlew publish\".")
    }

    val candidateProfiles = allProfiles.filter { group == it.name || group.startsWith(it.name) }

    if (candidateProfiles.isEmpty()) {
      throw IllegalArgumentException(
        "No matching staging profile found in account $username. It is expected that the account contains a staging " +
          "profile that matches or is the start of $group." +
          "Available profiles are: ${allProfiles.joinToString(separator = ", ") { it.name }}"
      )
    }

    if (candidateProfiles.size > 1) {
      throw IllegalArgumentException(
        "More than 1 matching staging profile found in account $username. " +
          "Available profiles are: ${allProfiles.joinToString(separator = ", ") { it.name }}"
      )
    }

    return candidateProfiles[0]
  }

  private fun createStagingRepository(group: String, profile: StagingProfile): String {
    println("Creating repository in profile: ${profile.name}")

    val response = service.createRepository(profile.id, CreateRepositoryInput(CreateRepositoryInputData("Repository for $group"))).execute()
    if (!response.isSuccessful) {
      throw IOException("Cannot create repository: ${response.errorBody()?.string()}")
    }

    val id = response.body()?.data?.stagedRepositoryId
    if (id == null) {
      throw IOException("Did not receive created repository")
    }

    println("Created staging repository $id")

    return id
  }

  fun createRepositoryForGroup(group: String): String {
    val profile = findStagingProfile(group)
    return createStagingRepository(group, profile)
  }

  private fun getProfileRepositories(): List<Repository>? {
    val profileRepositoriesResponse = service.getProfileRepositories().execute()

    if (!profileRepositoriesResponse.isSuccessful) {
      throw IOException("Cannot get profileRepositories for account $username: ${profileRepositoriesResponse.errorBody()?.string()}")
    }

    return profileRepositoriesResponse.body()?.data
  }

  private fun findStagingRepository(): Repository {
    val allRepositories = getProfileRepositories() ?: emptyList()

    if (allRepositories.isEmpty()) {
      throw IllegalArgumentException(
        "No matching staging repository found in account $username. You can can explicitly choose one by " +
          "passing it as an option like this \"./gradlew closeAndReleaseRepository --repository=comexample-123\". " +
          "Available repositories are: ${allRepositories.joinToString(separator = ", ") { it.repositoryId }}"
      )
    }

    if (allRepositories.size > 1) {
      throw IllegalArgumentException(
        "More than 1 matching staging repository found in account $username. You can can explicitly choose " +
          "one by passing it as an option like this \"./gradlew closeAndReleaseRepository --repository comexample-123\". " +
          "Available repositories are: ${allRepositories.joinToString(separator = ", ") { it.repositoryId }}"
      )
    }
    return allRepositories[0]
  }

  private fun getStagingRepository(repositoryId: String): Repository {
    val repositoryResponse = service.getRepository(repositoryId).execute()

    if (!repositoryResponse.isSuccessful) {
      throw IOException("Cannot get repository with id $repositoryId for account $username: ${repositoryResponse.errorBody()?.string()}")
    }

    val repository = repositoryResponse.body()

    if (repository == null) {
      throw IOException("Could not get repository with id $repositoryId for account $username")
    }

    return repository
  }

  private fun closeStagingRepository(stagingRepository: Repository): String {
    val repositoryId = stagingRepository.repositoryId

    if (stagingRepository.type != "open") {
      throw IllegalArgumentException("Repository $repositoryId is of type '${stagingRepository.type}' and not 'open'")
    }

    println("Closing repository: $repositoryId")
    val response = service.closeRepository(TransitionRepositoryInput(TransitionRepositoryInputData(listOf(repositoryId)))).execute()
    if (!response.isSuccessful) {
      throw IOException("Cannot close repository: ${response.errorBody()?.string()}")
    }

    waitForClose(repositoryId)

    return repositoryId
  }

  private fun waitForClose(repositoryId: String) {

    val startMillis = System.currentTimeMillis()

    val waitingChars = listOf(
      PROGRESS_1,
      PROGRESS_2,
      PROGRESS_3,
      PROGRESS_4,
      PROGRESS_5,
      PROGRESS_6,
      PROGRESS_7
    )
    var i = 0
    while (true) {
      if (System.currentTimeMillis() - startMillis > CLOSE_TIMEOUT_MILLIS) {
        throw IOException("Timeout waiting for repository close")
      }

      print("\r${waitingChars[i++ % waitingChars.size]} waiting for close...")
      System.out.flush()

      Thread.sleep(CLOSE_WAIT_INTERVAL_MILLIS)

      try {
        val repository = getStagingRepository(repositoryId)
        if (repository.type == "closed" && !repository.transitioning) {
          break
        }
      } catch (e: IOException) {
        System.err.println("Exception trying to get repository status: ${e.message}")
      } catch (e: TimeoutException) {
        System.err.println("Exception trying to get repository status: ${e.message}")
      }
    }
  }

  private fun releaseStagingRepository(repositoryId: String) {
    println("Releasing repository: $repositoryId")
    val response = service.releaseRepository(
      TransitionRepositoryInput(
        TransitionRepositoryInputData(
          stagedRepositoryIds = listOf(repositoryId),
          autoDropAfterRelease = true
        )
      )
    ).execute()

    if (!response.isSuccessful) {
      throw IOException("Cannot release repository: ${response.errorBody()?.string()}")
    }

    println("Repository $repositoryId released")
  }

  private fun closeAndReleaseRepository(stagingRepository: Repository) {
    closeStagingRepository(stagingRepository)
    releaseStagingRepository(stagingRepository.repositoryId)
  }

  fun closeAndReleaseCurrentRepository() {
    val stagingRepository = findStagingRepository()
    closeAndReleaseRepository(stagingRepository)
  }

  fun closeAndReleaseRepositoryById(repositoryId: String) {
    val stagingRepository = getStagingRepository(repositoryId)
    closeAndReleaseRepository(stagingRepository)
  }

  companion object {
    private const val PROGRESS_1 = "\u2839"
    private const val PROGRESS_2 = "\u2838"
    private const val PROGRESS_3 = "\u2834"
    private const val PROGRESS_4 = "\u2826"
    private const val PROGRESS_5 = "\u2807"
    private const val PROGRESS_6 = "\u280F"
    private const val PROGRESS_7 = "\u2819"

    private const val CLOSE_TIMEOUT_MILLIS = 15 * 60 * 1000L
    private const val CLOSE_WAIT_INTERVAL_MILLIS = 10_000L
  }
}
