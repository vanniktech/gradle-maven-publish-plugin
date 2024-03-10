package com.vanniktech.maven.publish.portal

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class SonatypeCentralPortal(
  private val baseUrl: String,
  private val usertoken: String,
  userAgentName: String,
  userAgentVersion: String,
  okhttpTimeoutSeconds: Long,
  private val closeTimeoutSeconds: Long,
) {
  private val service by lazy {
    val okHttpClient = OkHttpClient.Builder()
      .addInterceptor(SonatypeCentralPortalOkHttpInterceptor(usertoken, userAgentName, userAgentVersion))
      .connectTimeout(okhttpTimeoutSeconds, TimeUnit.SECONDS)
      .readTimeout(okhttpTimeoutSeconds, TimeUnit.SECONDS)
      .writeTimeout(okhttpTimeoutSeconds, TimeUnit.SECONDS)
      .build()
    val retrofit = Retrofit.Builder()
      .addConverterFactory(MoshiConverterFactory.create())
      .client(okHttpClient)
      .baseUrl(baseUrl)
      .build()

    retrofit.create(SonatypeCentralPortalService::class.java)
  }

  private fun deleteDeployment(deploymentId: String) {
    val deleteDeploymentResponse = service.deleteDeployment(deploymentId).execute()
    if (!deleteDeploymentResponse.isSuccessful) {
      throw IOException(
        "Failed to delete deploymentId $deploymentId code: ${deleteDeploymentResponse.code()} msg: ${
          deleteDeploymentResponse.errorBody()?.string()
        }",
      )
    }
  }

  private fun publishDeployment(deploymentId: String) {
    val publishDeploymentResponse = service.publishDeployment(deploymentId).execute()
    if (!publishDeploymentResponse.isSuccessful) {
      throw IOException(
        "Failed to delete deploymentId $deploymentId code: ${publishDeploymentResponse.code()} msg: ${
          publishDeploymentResponse.errorBody()?.string()
        }",
      )
    }
  }

  // trying logic outlined on: https://www.megumethod.com/blog/downloading-files-retrofit-library
  private suspend fun getDeploymentDownloadByIdAndPath(deploymentId: String, relativePath: String, outputPath: String): File {
    val destinationFile = File(outputPath)
    val response = service.getDeploymentDownloadByIdAndPath(deploymentId, relativePath).execute()
    if (response.isSuccessful) {
      response.body()?.byteStream().use { inputStream ->
        destinationFile.outputStream().use { outputStream ->
          inputStream?.copyTo(outputStream)
        }
      }
    } else {
      throw IOException(
        "Failed to retrieve content for $deploymentId on relativePath: $relativePath. msg: ${
          response.errorBody()?.string()
        }",
      )
    }
    return destinationFile
  }

  private suspend fun getDeploymentDownload(relativePath: String, outputPath: String): File {
    val destinationFile = File(outputPath)
    val response = service.getDeploymentDownload(relativePath).execute()
    if (response.isSuccessful) {
      response.body()?.byteStream().use { inputStream ->
        destinationFile.outputStream().use { outputStream ->
          inputStream?.copyTo(outputStream)
        }
      }
    } else {
      throw IOException(
        "Failed to retrieve content on relativePath: $relativePath. msg: ${
          response.errorBody()?.string()
        }",
      )
    }
    return destinationFile
  }

  private fun getPublished(namespace: String, name: String, version: String): String? {
    val stringResponse = service.getPublished(namespace, name, version).execute()
    if (stringResponse.isSuccessful) {
      return stringResponse.body()
    } else {
      throw IOException(
        "Failed to get published status for $namespace:$name:$version. msg: ${
          stringResponse.errorBody()?.string()
        }",
      )
    }
  }

  private fun getStatus(deploymentId: String): DeploymentStatus {
    val statusResponse = service.getStatus(deploymentId).execute()
    if (statusResponse.isSuccessful) {
      return statusResponse.body()!!
    } else {
      throw IOException("Failed to get status for $deploymentId. msg: ${statusResponse.errorBody()?.string()}")
    }
  }

  private fun upload(name: String?, publishingType: String?, file: File): String {
    val uploadFile: RequestBody = file.asRequestBody("application/octet-stream".toMediaType())
    val multipart =
      MultipartBody.Part.createFormData("bundle", file.getName(), uploadFile)
    val uploadResponse = service.uploadBundle(name, publishingType, multipart).execute()
    if (uploadResponse.isSuccessful) {
      return uploadResponse.body()!!
    } else {
      throw IOException("Upload failed: ${uploadResponse.errorBody()?.string()}")
    }
  }
}
