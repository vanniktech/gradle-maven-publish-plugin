package com.vanniktech.maven.publish.central

internal sealed interface EndOfBuildAction {
  val runAfterFailure: Boolean

  object Upload : EndOfBuildAction {
    override val runAfterFailure: Boolean = false
  }

  object Publish : EndOfBuildAction {
    override val runAfterFailure: Boolean = false
  }

  object Validate : EndOfBuildAction {
    override val runAfterFailure: Boolean = false
  }

  data class Drop(
    override val runAfterFailure: Boolean,
  ) : EndOfBuildAction
}
