package com.vanniktech.maven.publish.portal

import okhttp3.Interceptor
import okhttp3.Response

internal class SonatypeCentralPortalOkHttpInterceptor(
  private val usertoken: String,
  private val userAgentName: String,
  private val userAgentVersion: String,
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val requestBuilder = chain.request().newBuilder()

    requestBuilder.addHeader("Accept", "application/json") // request json by default, XML is returned else
    requestBuilder.addHeader("Authorization", "UserToken $usertoken")
    requestBuilder.addHeader("User-Agent", "$userAgentName/$userAgentVersion")

    return chain.proceed(requestBuilder.build())
  }
}
