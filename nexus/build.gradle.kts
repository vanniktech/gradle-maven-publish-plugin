plugins {
  id("shared")
}

dependencies {
  ksp(libs.moshi.codegen)

  implementation(libs.okhttp)
  implementation(libs.moshi)
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.moshi)

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.engine)
  testImplementation(libs.junit.launcher)
  testImplementation(libs.truth)
  testImplementation(libs.truth.java8)
}

tasks.withType(Test::class.java).configureEach {
  useJUnitPlatform()
}
