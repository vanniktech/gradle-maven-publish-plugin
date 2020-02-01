package com.vanniktech.maven.publish

internal interface Configurer {
  /**
   * Needs to be called for all targets before `addComponent` and
   * `addTaskOutput`.
   */
  fun configureTarget(target: MavenPublishTarget)

  fun configureMultiplatformProject()

  fun configureAndroidArtifacts()

  fun configureJavaArtifacts()
}
