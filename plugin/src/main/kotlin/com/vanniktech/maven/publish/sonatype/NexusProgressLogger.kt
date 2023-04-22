package com.vanniktech.maven.publish.sonatype

import com.vanniktech.maven.publish.nexus.Nexus
import org.gradle.internal.logging.progress.ProgressLogger

internal class NexusProgressLogger(
  override val usingPlainConsole: Boolean,
  private val progressLogger: ProgressLogger,
) : Nexus.Logger {
  override fun start(description: String, status: String) {
    progressLogger.start(description, status)
  }

  override fun lifecycle(status: String) {
    // Lifecycle events don't participate in progress logging.
    println(status)
  }

  override fun progress(status: String, failing: Boolean) {
    progressLogger.progress(status, failing)
  }

  override fun completed(status: String, failed: Boolean) {
    progressLogger.completed(status, failed)
  }
}
