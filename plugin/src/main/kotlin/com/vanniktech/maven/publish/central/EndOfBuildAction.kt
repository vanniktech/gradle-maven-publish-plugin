package com.vanniktech.maven.publish.central

internal sealed interface EndOfBuildAction {
  val runAfterFailure: Boolean

  data class Close(
    val searchForRepositoryIfNoIdPresent: Boolean,
  ) : EndOfBuildAction {
    override val runAfterFailure: Boolean = false
  }

  object ReleaseAfterClose : EndOfBuildAction {
    override val runAfterFailure: Boolean = false
  }

  data class Drop(
    override val runAfterFailure: Boolean,
    val searchForRepositoryIfNoIdPresent: Boolean,
  ) : EndOfBuildAction
}
