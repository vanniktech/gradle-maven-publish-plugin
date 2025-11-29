plugins {
  id("shared")
}

dependencies {
  implementation(libs.okhttp.client)
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.scalars)
  implementation(libs.retrofit.converter.moshi)
  implementation(libs.slf4j.api)
  implementation(libs.moshi)
  implementation(libs.moshi.kotlin)

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.engine)
  testImplementation(libs.junit.launcher)
  testImplementation(libs.truth)
  testImplementation(libs.slf4j.api)
  testImplementation(libs.okhttp.mockwebserver)

  testRuntimeOnly(libs.slf4j.simple)
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}
