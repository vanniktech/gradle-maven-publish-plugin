package com.vanniktech.maven.publish

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.plugins.signing.SigningExtension
import java.util.concurrent.Callable

internal val Project.signing get() = extensions.getByType(SigningExtension::class.java)
internal val Project.publishing get() = extensions.getByType(PublishingExtension::class.java)

internal val Project.isSigningRequired
  get() = Callable<Boolean> { !project.version.toString().contains("SNAPSHOT") }
