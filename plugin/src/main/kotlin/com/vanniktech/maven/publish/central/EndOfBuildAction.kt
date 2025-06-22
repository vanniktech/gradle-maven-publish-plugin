package com.vanniktech.maven.publish.central

internal sealed interface EndOfBuildAction {
  val runAfterFailure: Boolean

  data object Upload : EndOfBuildAction {
    override val runAfterFailure: Boolean = false
  }

  data object Publish : EndOfBuildAction {
    override val runAfterFailure: Boolean = false
  }

  data class Drop(
    override val runAfterFailure: Boolean,
  ) : EndOfBuildAction
}
