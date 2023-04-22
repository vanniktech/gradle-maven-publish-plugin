package com.vanniktech.maven.publish.nexus

import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class Nexus(
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
      throw IOException("Cannot get stagingProfiles for account $username: ${stagingProfilesResponse.errorBody()?.string()}")
    }

    return stagingProfilesResponse.body()?.data
  }

  private fun findStagingProfile(group: String): StagingProfile {
    val allProfiles = getProfiles() ?: emptyList()

    if (allProfiles.isEmpty()) {
      throw IllegalArgumentException("No staging profiles found in account $username. Make sure you called \"./gradlew publish\".")
    }

    if (allProfiles.size == 1) {
      return allProfiles[0]
    }

    val candidateProfiles = allProfiles.filter { group == it.name }
      .ifEmpty { allProfiles.filter { group.startsWith(it.name) } }
      .ifEmpty { allProfiles.filter { group.commonPrefixWith(it.name).isNotEmpty() } }

    if (candidateProfiles.isEmpty()) {
      throw IllegalArgumentException(
        "No matching staging profile found in account $username. It is expected that the account contains a staging " +
          "profile that matches or is the start of $group. " +
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

  private fun createStagingRepository(group: String, profile: StagingProfile, logger: Logger): String {
    logger.start("Create staging repository", "Creating in profile '${profile.name}'")

    val response = service.createRepository(profile.id, CreateRepositoryInput(CreateRepositoryInputData("Repository for $group"))).execute()
    if (!response.isSuccessful) {
      throw IOException("Cannot create repository: ${response.errorBody()?.string()}")
    }

    val id = response.body()?.data?.stagedRepositoryId ?: throw IOException("Did not receive created repository")

    logger.completed("Created staging repository $id", failed = false)

    return id
  }

  fun createRepositoryForGroup(group: String, logger: Logger): String {
    val profile = findStagingProfile(group)
    return createStagingRepository(group, profile, logger)
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

    return repositoryResponse.body()
      ?: throw IOException("Could not get repository with id $repositoryId for account $username")
  }

  private fun closeStagingRepository(stagingRepository: Repository, logger: Logger) {
    val repositoryId = stagingRepository.repositoryId

    if (stagingRepository.type == "closed") {
      if (stagingRepository.transitioning) {
        waitForClose(stagingRepository.repositoryId, logger)
      } else {
        logger.lifecycle("Repository $repositoryId already closed")
      }
      return
    }

    if (stagingRepository.type != "open") {
      throw IllegalArgumentException("Repository $repositoryId is of type '${stagingRepository.type}' and not 'open'")
    }

    logger.lifecycle("Closing repository: $repositoryId")
    val response = service.closeRepository(TransitionRepositoryInput(TransitionRepositoryInputData(listOf(repositoryId)))).execute()
    if (!response.isSuccessful) {
      throw IOException("Cannot close repository: ${response.errorBody()?.string()}")
    }

    waitForClose(repositoryId, logger)
    logger.lifecycle("Repository $repositoryId closed")
  }

  private fun waitForClose(repositoryId: String, logger: Logger) {

    val startMillis = System.currentTimeMillis()

    val progressUpdateInternal = if (logger.usingPlainConsole) {
      CLOSE_PROGRESS_UPDATE_INTERVAL_MILLIS_SLOW
    } else {
      CLOSE_PROGRESS_UPDATE_INTERVAL_MILLIS_FAST
    }
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
    var lastNetworkCheck = System.currentTimeMillis()
    while (true) {
      if (System.currentTimeMillis() - startMillis > TimeUnit.SECONDS.toMillis(closeTimeoutSeconds)) {
        throw IOException("Timeout waiting for repository close")
      }

      logger.progress("${waitingChars[i++ % waitingChars.size]} waiting for close...")

      Thread.sleep(progressUpdateInternal)

      val newTime = System.currentTimeMillis()
      if (newTime - lastNetworkCheck < CLOSE_CLOSE_CHECK_INTERVAL_MILLIS) {
        // Not enough time has lapsed to re-check the repository
        continue
      }
      lastNetworkCheck = newTime
      val repository = try {
        getStagingRepository(repositoryId)
      } catch (e: IOException) {
        logger.progress("Exception trying to get repository status: ${e.message}", failing = true)
        null
      } catch (e: TimeoutException) {
        logger.progress("Exception trying to get repository status: ${e.message}", failing = true)
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

  fun closeCurrentStagingRepository(logger: Logger): String {
    val stagingRepository = findStagingRepository()
    closeStagingRepository(stagingRepository, logger)
    return stagingRepository.repositoryId
  }

  fun closeStagingRepository(repositoryId: String, logger: Logger) {
    val stagingRepository = getStagingRepository(repositoryId)
    closeStagingRepository(stagingRepository, logger)
  }

  fun releaseStagingRepository(repositoryId: String, logger: Logger) {
    logger.progress("Releasing repository: $repositoryId")
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

    logger.completed("Repository $repositoryId released", failed = false)
  }

  fun dropStagingRepository(repositoryId: String) {
    val response = service.dropRepository(
      TransitionRepositoryInput(
        TransitionRepositoryInputData(
          stagedRepositoryIds = listOf(repositoryId)
        )
      )
    ).execute()

    if (!response.isSuccessful) {
      throw IOException("Cannot drop repository: ${response.errorBody()?.string()}")
    }
  }

  fun dropCurrentStagingRepository() {
    val stagingRepository = findStagingRepository()
    dropStagingRepository(stagingRepository.repositoryId)
  }

  /** A simple logger interface that can start, complete, and report intermediate progress. */
  interface Logger {
    /**
     * Indicates if this progress logger is outputting to a plain console output, which is
     * useful to know if it can handle more frequent progress update ticks.
     */
    val usingPlainConsole: Boolean
      get() = false

    fun start(description: String, status: String)
    fun lifecycle(status: String)
    fun progress(status: String, failing: Boolean = false)
    fun completed(status: String, failed: Boolean)

    /** A system logger that writes to [System.out] or [System.err]. */
    object SystemLogger : Logger {
      private fun flush() {
        System.err.flush()
        System.out.flush()
      }

      override fun start(description: String, status: String) {
        println("$description: $status")
      }

      override fun lifecycle(status: String) {
        flush()
        println(status)
      }

      override fun progress(status: String, failing: Boolean) {
        if (failing) {
          System.err.print("\r$status")
        } else {
          print("\r$status")
        }
        flush()
      }
      override fun completed(status: String, failed: Boolean) {
        flush()
        if (failed) {
          System.err.println("Completed with errors: $status\n")
        } else {
          println("Completed: $status\n")
        }
      }
    }
  }

  companion object {
    private const val PROGRESS_1 = "\u2839"
    private const val PROGRESS_2 = "\u2838"
    private const val PROGRESS_3 = "\u2834"
    private const val PROGRESS_4 = "\u2826"
    private const val PROGRESS_5 = "\u2807"
    private const val PROGRESS_6 = "\u280F"
    private const val PROGRESS_7 = "\u2819"

    /** Update the progress loader every 500ms, for rich loggers. */
    private const val CLOSE_PROGRESS_UPDATE_INTERVAL_MILLIS_FAST = 200L
    /** Update the progress loader every 10s, for non-rich loggers. */
    private const val CLOSE_PROGRESS_UPDATE_INTERVAL_MILLIS_SLOW = 200L
    /** Check the repository every 5 seconds. */
    private const val CLOSE_CLOSE_CHECK_INTERVAL_MILLIS = 5_000L
  }
}
