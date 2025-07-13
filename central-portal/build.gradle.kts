plugins {
  id("shared")
}

dependencies {
  implementation(libs.okhttp)
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.scalars)
}
