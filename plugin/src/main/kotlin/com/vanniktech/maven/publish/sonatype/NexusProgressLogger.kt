package com.vanniktech.maven.publish.sonatype

import com.vanniktech.maven.publish.nexus.Nexus
import org.gradle.internal.logging.progress.ProgressLogger

internal class NexusProgressLogger(private val progressLogger: ProgressLogger) : Nexus.Logger {
  override fun start(description: String, status: String) {
    progressLogger.start(description, status)
  }

  override fun progress(status: String, failing: Boolean) {
    progressLogger.progress(status, failing)
  }

  override fun completed(status: String, failed: Boolean) {
    progressLogger.completed(status, failed)
  }
}
