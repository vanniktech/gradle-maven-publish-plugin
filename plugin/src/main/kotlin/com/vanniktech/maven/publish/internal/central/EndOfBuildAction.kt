package com.vanniktech.maven.publish.internal.central

internal sealed interface EndOfBuildAction {
  val runAfterFailure: Boolean

  object Upload : EndOfBuildAction {
    override val runAfterFailure: Boolean = false
  }

  object Publish : EndOfBuildAction {
    override val runAfterFailure: Boolean = false
  }

  data class Drop(
    override val runAfterFailure: Boolean,
  ) : EndOfBuildAction
}
