package com.vanniktech.maven.publish.nexus

import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

public class Nexus(
  private val baseUrl: String,
  private val username: String,
  password: String,
  userAgentName: String,
  userAgentVersion: String,
  okhttpTimeoutSeconds: Long,
  private val closeTimeoutSeconds: Long,
) {
  private val service by lazy {
    val okHttpClient = OkHttpClient.Builder()
      .addInterceptor(NexusOkHttpInterceptor(username, password, userAgentName, userAgentVersion))
      .connectTimeout(okhttpTimeoutSeconds, TimeUnit.SECONDS)
      .readTimeout(okhttpTimeoutSeconds, TimeUnit.SECONDS)
      .writeTimeout(okhttpTimeoutSeconds, TimeUnit.SECONDS)
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
      throw IOException(
        "Cannot get stagingProfiles for account $username: " +
          "(${stagingProfilesResponse.code()}) " +
          "${stagingProfilesResponse.errorBody()?.string()}",
      )
    }

    return stagingProfilesResponse.body()?.data
  }

  private fun findStagingProfile(group: String): StagingProfile {
    val allProfiles = getProfiles() ?: emptyList()
    return allProfiles.findStagingProfileForGroup(group, username)
  }

  private fun createStagingRepository(group: String, profile: StagingProfile): String {
    println("Creating repository in profile: ${profile.name}")

    val response = service.createRepository(profile.id, CreateRepositoryInput(CreateRepositoryInputData("Repository for $group"))).execute()
    if (!response.isSuccessful) {
      throw IOException("Cannot create repository: ${response.errorBody()?.string()}")
    }

    val id = response.body()?.data?.stagedRepositoryId ?: throw IOException("Did not receive created repository")

    println("Created staging repository $id")

    return id
  }

  public fun createRepositoryForGroup(group: String): String {
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
          "Available repositories are: ${allRepositories.joinToString(separator = ", ") { it.repositoryId }}",
      )
    }

    if (allRepositories.size > 1) {
      throw IllegalArgumentException(
        "More than 1 matching staging repository found in account $username. You can can explicitly choose " +
          "one by passing it as an option like this \"./gradlew closeAndReleaseRepository --repository comexample-123\". " +
          "Available repositories are: ${allRepositories.joinToString(separator = ", ") { it.repositoryId }}",
      )
    }
    return allRepositories[0]
  }

  private fun getStagingRepository(repositoryId: String): Repository {
    val repositoryResponse = service.getRepository(repositoryId).execute()

    if (!repositoryResponse.isSuccessful) {
      throw IOException("Cannot get repository with id $repositoryId for account $username: ${repositoryResponse.errorBody()?.string()}")
    }

    return repositoryResponse.body()
      ?: throw IOException("Could not get repository with id $repositoryId for account $username")
  }

  private fun closeStagingRepository(stagingRepository: Repository) {
    val repositoryId = stagingRepository.repositoryId

    if (stagingRepository.type == "closed") {
      if (stagingRepository.transitioning) {
        waitForClose(stagingRepository.repositoryId)
      } else {
        println("Repository $repositoryId already closed")
      }
      return
    }

    if (stagingRepository.type != "open") {
      throw IllegalArgumentException("Repository $repositoryId is of type '${stagingRepository.type}' and not 'open'")
    }

    println("Closing repository: $repositoryId")
    val response = service.closeRepository(TransitionRepositoryInput(TransitionRepositoryInputData(listOf(repositoryId)))).execute()
    if (!response.isSuccessful) {
      throw IOException("Cannot close repository: ${response.errorBody()?.string()}")
    }

    waitForClose(repositoryId)
    println("Repository $repositoryId closed")
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
      PROGRESS_7,
    )
    var i = 0
    while (true) {
      if (System.currentTimeMillis() - startMillis > TimeUnit.SECONDS.toMillis(closeTimeoutSeconds)) {
        throw IOException("Timeout waiting for repository close")
      }

      print("\r${waitingChars[i++ % waitingChars.size]} waiting for close...")
      System.out.flush()

      Thread.sleep(CLOSE_WAIT_INTERVAL_MILLIS)

      val repository = try {
        getStagingRepository(repositoryId)
      } catch (e: IOException) {
        System.err.println("Exception trying to get repository status: ${e.message}")
        null
      } catch (e: TimeoutException) {
        System.err.println("Exception trying to get repository status: ${e.message}")
        null
      }

      if (repository?.type == "closed" && !repository.transitioning) {
        break
      }
      if (repository?.type == "open" && !repository.transitioning && repository.notifications > 0) {
        val properties = try {
          val response = service.getRepositoryActivity(repositoryId).execute()
          if (response.isSuccessful) {
            response.body()?.find { it.name == "close" }
              ?.events?.find { it.name == "ruleFailed" }
              ?.properties?.filter { it.name == "failureMessage" }
          } else {
            emptyList()
          }
        } catch (_: IOException) {
          emptyList()
        }

        if (properties.isNullOrEmpty()) {
          val url = baseUrl.toHttpUrl().newBuilder("/#stagingRepositories").toString()
          throw IOException("Closing the repository failed. ${repository.notifications} messages are available on $url")
        } else {
          val message = properties.joinToString("\n") { it.value }
          throw IOException("Closing the repository failed with the following errors:\n$message")
        }
      }
    }
  }

  public fun closeCurrentStagingRepository(): String {
    val stagingRepository = findStagingRepository()
    closeStagingRepository(stagingRepository)
    return stagingRepository.repositoryId
  }

  public fun closeStagingRepository(repositoryId: String) {
    val stagingRepository = getStagingRepository(repositoryId)
    closeStagingRepository(stagingRepository)
  }

  public fun releaseStagingRepository(repositoryId: String) {
    println("Releasing repository: $repositoryId")
    val response = service.releaseRepository(
      TransitionRepositoryInput(
        TransitionRepositoryInputData(
          stagedRepositoryIds = listOf(repositoryId),
          autoDropAfterRelease = true,
        ),
      ),
    ).execute()

    if (!response.isSuccessful) {
      throw IOException("Cannot release repository: ${response.errorBody()?.string()}")
    }

    println("Repository $repositoryId released")
  }

  public fun dropStagingRepository(repositoryId: String) {
    val response = service.dropRepository(
      TransitionRepositoryInput(
        TransitionRepositoryInputData(
          stagedRepositoryIds = listOf(repositoryId),
        ),
      ),
    ).execute()

    if (!response.isSuccessful) {
      throw IOException("Cannot drop repository: ${response.errorBody()?.string()}")
    }
  }

  public fun dropCurrentStagingRepository() {
    val stagingRepository = findStagingRepository()
    dropStagingRepository(stagingRepository.repositoryId)
  }

  public companion object {
    private const val PROGRESS_1 = "\u2839"
    private const val PROGRESS_2 = "\u2838"
    private const val PROGRESS_3 = "\u2834"
    private const val PROGRESS_4 = "\u2826"
    private const val PROGRESS_5 = "\u2807"
    private const val PROGRESS_6 = "\u280F"
    private const val PROGRESS_7 = "\u2819"

    private const val CLOSE_WAIT_INTERVAL_MILLIS = 10_000L
  }
}
