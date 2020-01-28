package com.vanniktech.maven.publish.nexus

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class NexusOkHttpInterceptor(val username: String, val password: String) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val requestBuilder = chain.request().newBuilder()

    requestBuilder.addHeader("Accept", "application/json") // request json by default, XML is returned else
    requestBuilder.addHeader("Authorization", Credentials.basic(username, password))

    return chain.proceed(requestBuilder.build())
  }
}
