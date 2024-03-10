plugins {
  id("shared")
}

dependencies {
  kapt(libs.moshi.codegen)

  implementation(libs.okhttp)
  implementation(libs.moshi)
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.moshi)
  implementation(libs.retrofit.converter.scalars)
}
