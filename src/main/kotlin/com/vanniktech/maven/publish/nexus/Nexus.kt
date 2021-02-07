package com.vanniktech.maven.publish.nexus

import com.vanniktech.maven.publish.nexus.model.Repository
import com.vanniktech.maven.publish.nexus.model.TransitionRepositoryInput
import com.vanniktech.maven.publish.nexus.model.TransitionRepositoryInputData
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

internal class Nexus(
  baseUrl: String,
  username: String,
  password: String,
  private val stagingProfile: String?,
  private val stagingRepository: String?
) {
  private val service by lazy {
    val okHttpClient = OkHttpClient.Builder()
      .addInterceptor(NexusOkHttpInterceptor(username, password))
      .build()
    val retrofit = Retrofit.Builder()
      .addConverterFactory(MoshiConverterFactory.create())
      .client(okHttpClient)
      .baseUrl(baseUrl)
      .build()

    retrofit.create(NexusService::class.java)
  }

  private fun getProfileRepositories(): List<Repository>? {
    val profileRepositoriesResponse = service.getProfileRepositories().execute()

    if (!profileRepositoriesResponse.isSuccessful) {
      throw IOException("Cannot get profileRepositories: ${profileRepositoriesResponse.errorBody()?.string()}")
    }

    return profileRepositoriesResponse.body()?.data
  }

  @Suppress("ThrowsCount")
  private fun findStagingRepository(): Repository {
    val allRepositories = getProfileRepositories() ?: emptyList()

    if (allRepositories.isEmpty()) {
      throw IllegalArgumentException("No staging repository prefixed with. Make sure you called \"./gradlew publish\".")
    }

    val candidateRepositories = when {
      stagingRepository != null -> allRepositories.filter { it.repositoryId == stagingRepository }
      stagingProfile != null -> allRepositories.filter { it.repositoryId.startsWith(stagingProfile.replace(".", "")) }
      else -> allRepositories
    }

    if (candidateRepositories.isEmpty()) {
      throw IllegalArgumentException("No matching staging repository found. You can can explicitly choose one by " +
        "passing it as an option like this \"./gradlew closeAndReleaseRepository --repository=comexample-123\". " +
        "Available repositories are: ${allRepositories.joinToString(separator = ", ") { it.repositoryId }}")
    }

    if (candidateRepositories.size > 1) {
      throw IllegalArgumentException("More than 1 matching staging repository found. You can can explicitly choose " +
        "one by passing it as an option like this \"./gradlew closeAndReleaseRepository --repository=comexample-123\". " +
        "Available repositories are: ${allRepositories.joinToString(separator = ", ") { it.repositoryId }}")
    }
    return candidateRepositories[0]
  }

  fun findAndCloseStagingRepository(): String {
    val stagingRepository = findStagingRepository()
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
        val repository = service.getRepository(repositoryId).execute().body()
        if (repository?.type == "closed" && !repository.transitioning) {
          break
        }
      } catch (e: IOException) {
        System.err.println("Exception trying to get repository status: ${e.message}")
      }
    }
  }

  fun releaseStagingRepository(repositoryId: String) {
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

  fun closeAndReleaseRepository() {
    val repositoryId = findAndCloseStagingRepository()
    releaseStagingRepository(repositoryId)
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
