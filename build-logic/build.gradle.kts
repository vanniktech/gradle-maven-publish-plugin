plugins { `kotlin-dsl` }

dependencies {
  implementation(libs.kotlin.plugin)
  implementation(libs.maven.publish.plugin)
  implementation(libs.bcv)
  // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
