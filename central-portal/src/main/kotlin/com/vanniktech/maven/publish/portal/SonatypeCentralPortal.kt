package com.vanniktech.maven.publish.portal

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import org.slf4j.Logger
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

public class SonatypeCentralPortal(
  private val baseUrl: String,
  private val usertoken: String,
  userAgentName: String,
  userAgentVersion: String,
  okhttpTimeoutSeconds: Long,
  private val closeTimeoutSeconds: Long,
  private val pollIntervalMs: Long,
  private val logger: Logger,
) {
  private val service by lazy {
    val moshi = Moshi
      .Builder()
      .add(KotlinJsonAdapterFactory())
      .build()

    val okHttpClient = OkHttpClient
      .Builder()
      .addInterceptor(SonatypeCentralPortalOkHttpInterceptor(usertoken, userAgentName, userAgentVersion))
      .connectTimeout(okhttpTimeoutSeconds, TimeUnit.SECONDS)
      .readTimeout(okhttpTimeoutSeconds, TimeUnit.SECONDS)
      .writeTimeout(okhttpTimeoutSeconds, TimeUnit.SECONDS)
      .build()
    val retrofit = Retrofit
      .Builder()
      .client(okHttpClient)
      .baseUrl(baseUrl)
      .addConverterFactory(ScalarsConverterFactory.create())
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()

    retrofit.create(SonatypeCentralPortalService::class.java)
  }

  public fun deleteDeployment(deploymentId: String) {
    val deleteDeploymentResponse = service.deleteDeployment(deploymentId).execute()
    if (!deleteDeploymentResponse.isSuccessful) {
      throw IOException(
        "Failed to delete deploymentId $deploymentId code: ${deleteDeploymentResponse.code()} msg: ${
          deleteDeploymentResponse.errorBody()?.string()
        }",
      )
    }
  }

  public fun publishDeployment(deploymentId: String) {
    val publishDeploymentResponse = service.publishDeployment(deploymentId).execute()
    if (!publishDeploymentResponse.isSuccessful) {
      throw IOException(
        "Failed to delete deploymentId $deploymentId code: ${publishDeploymentResponse.code()} msg: ${
          publishDeploymentResponse.errorBody()?.string()
        }",
      )
    }
  }

  public fun upload(name: String, publishingType: PublishingType, file: File): String {
    val uploadFile = file.asRequestBody("application/octet-stream".toMediaType())
    val multipart = MultipartBody.Part.createFormData("bundle", file.name, uploadFile)
    val uploadResponse = service.uploadBundle(name, publishingType, multipart).execute()
    if (uploadResponse.isSuccessful) {
      return requireNotNull(uploadResponse.body())
    } else {
      throw IOException("Upload failed: ${uploadResponse.errorBody()?.string()}")
    }
  }

  /**
   * Validates the deployment by polling its status until it reaches `PUBLISHED` or `FAILED`.
   *
   * @param deploymentId The ID of the deployment to validate
   * @param logger An SLF4J logger instance for logging deployment status updates
   * @throws IOException if the deployment fails validation or an API error occurs
   */
  @OptIn(ExperimentalTime::class)
  public fun validateDeployment(deploymentId: String) {
    val startMark = TimeSource.Monotonic.markNow()
    val timeout = closeTimeoutSeconds.seconds
    var lastState: DeploymentState? = null

    logger.warn("Validating deployment $deploymentId...")

    while (startMark.elapsedNow() < timeout) {
      val statusResponse = service.checkDeploymentStatus(deploymentId).execute()

      if (!statusResponse.isSuccessful) {
        throw IOException(
          "Failed to check deployment status for $deploymentId code: ${statusResponse.code()} msg: ${
            statusResponse.errorBody()?.string()
          }",
        )
      }

      val status = requireNotNull(statusResponse.body()) {
        "Status response body is null for deployment $deploymentId"
      }

      if (status.deploymentState != lastState) {
        lastState = status.deploymentState

        when (status.deploymentState) {
          DeploymentState.PENDING -> logger.warn("Deployment is pending validation")
          DeploymentState.VALIDATING -> logger.warn("Deployment is being validated")
          DeploymentState.VALIDATED -> logger.warn("Deployment has been validated successfully")
          DeploymentState.PUBLISHING -> logger.warn("Deployment is being published to Maven Central")

          DeploymentState.PUBLISHED -> {
            logger.warn("Deployment has been published to Maven Central")
            return
          }

          DeploymentState.FAILED -> {
            val errorMessages =
              status.errors?.entries?.joinToString("\n") { (publication, errors) ->
                buildString {
                  appendLine("Publication $publication:")
                  errors.forEach { error ->
                    appendLine("* $error")
                  }
                }
              } ?: "No error details available"

            throw IOException(
              "Deployment $deploymentId failed validation:\n$errorMessages",
            )
          }
        }
      }

      Thread.sleep(pollIntervalMs)
    }

    throw IOException(
      "Deployment validation timed out after ${closeTimeoutSeconds}s. " +
        "Last known state: ${lastState ?: "UNKNOWN"}",
    )
  }

  public enum class PublishingType {
    AUTOMATIC,
    USER_MANAGED,
  }
}
