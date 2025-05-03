plugins { `kotlin-dsl` }

dependencies {
  implementation(libs.kotlin.plugin)
  implementation(libs.ktlint.plugin)
  implementation(libs.maven.publish.plugin)
}
