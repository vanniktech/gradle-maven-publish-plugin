package com.vanniktech.maven.publish.portal

import java.io.File
import java.io.IOException
import kotlin.time.Duration
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

public class SonatypeCentralPortal(
  private val baseUrl: String,
  private val userToken: String,
  userAgentName: String,
  userAgentVersion: String,
  okhttpTimeout: Duration,
) {
  private val service by lazy {
    val okHttpClient = OkHttpClient
      .Builder()
      .addInterceptor(SonatypeCentralPortalOkHttpInterceptor(userToken, userAgentName, userAgentVersion))
      .connectTimeout(okhttpTimeout)
      .readTimeout(okhttpTimeout)
      .writeTimeout(okhttpTimeout)
      .build()
    val retrofit = Retrofit
      .Builder()
      .client(okHttpClient)
      .baseUrl(baseUrl)
      .addConverterFactory(ScalarsConverterFactory.create())
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
    val uploadFile = RequestBody.create(MediaType.parse("application/octet-stream"), file)
    val multipart = MultipartBody.Part.createFormData("bundle", file.name, uploadFile)
    val uploadResponse = service.uploadBundle(name, publishingType, multipart).execute()
    if (uploadResponse.isSuccessful) {
      return requireNotNull(uploadResponse.body()) { "Upload response body should never be null" }
    } else {
      throw IOException("Upload failed: ${uploadResponse.errorBody()?.string()}")
    }
  }

  public enum class PublishingType {
    AUTOMATIC,
    USER_MANAGED,
  }
}
