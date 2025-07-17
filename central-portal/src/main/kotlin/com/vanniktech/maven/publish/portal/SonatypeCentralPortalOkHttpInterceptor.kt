package com.vanniktech.maven.publish.portal

import okhttp3.Interceptor
import okhttp3.Response

internal class SonatypeCentralPortalOkHttpInterceptor(
  private val userToken: String,
  private val userAgentName: String,
  private val userAgentVersion: String,
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val requestBuilder = chain
      .request()
      .newBuilder()
      .addHeader("Accept", "application/json") // request json by default, XML is returned else
      .addHeader("Authorization", "Bearer $userToken")
      .addHeader("User-Agent", "$userAgentName/$userAgentVersion")

    return chain.proceed(requestBuilder.build())
  }
}
