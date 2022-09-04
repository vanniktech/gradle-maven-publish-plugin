plugins {
  id("shared")
}

dependencies {
  add("kapt", "com.squareup.moshi:moshi-kotlin-codegen:${Version.moshi}")

  implementation("com.squareup.okhttp3:okhttp:${Version.okhttp}")
  implementation("com.squareup.moshi:moshi:${Version.moshi}")
  implementation("com.squareup.retrofit2:retrofit:${Version.retrofit}")
  implementation("com.squareup.retrofit2:converter-moshi:${Version.retrofit}")
}
