plugins {
  id("shared")
}

dependencies {
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.scalars)
}
