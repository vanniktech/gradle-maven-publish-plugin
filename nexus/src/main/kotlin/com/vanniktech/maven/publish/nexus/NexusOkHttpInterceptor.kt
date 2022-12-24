package com.vanniktech.maven.publish.nexus

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

internal class NexusOkHttpInterceptor(
  private val username: String,
  private val password: String,
  private val userAgentName: String,
  private val userAgentVersion: String,
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val requestBuilder = chain.request().newBuilder()

    requestBuilder.addHeader("Accept", "application/json") // request json by default, XML is returned else
    requestBuilder.addHeader("Authorization", Credentials.basic(username, password))
    requestBuilder.addHeader("User-Agent", "$userAgentName/$userAgentVersion")

    return chain.proceed(requestBuilder.build())
  }
}
