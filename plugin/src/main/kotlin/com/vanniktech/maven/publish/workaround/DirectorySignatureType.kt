package com.vanniktech.maven.publish.workaround

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.plugins.signing.signatory.Signatory
import org.gradle.plugins.signing.type.AbstractSignatureType
import org.gradle.plugins.signing.type.SignatureType

/**
 * Creates signature files in a separate given directory to avoid
 * Gradle complaining about task depedencies missing when running
 * KMP tests and signing in the same build.
 *
 * https://youtrack.jetbrains.com/issue/KT-61313/
 * https://github.com/gradle/gradle/issues/26132
 */
public class DirectorySignatureType(
  @Nested
  public val actual: SignatureType,
  @Internal
  public val directory: Provider<Directory>,
) : AbstractSignatureType() {
  override fun fileFor(toSign: File): File {
    val original = super.fileFor(toSign)
    return directory.get().file(original.name).asFile
  }

  override fun sign(signatory: Signatory?, toSign: File?): File {
    // needs to call super and not actual because this is what will call fileFor
    return super.sign(signatory, toSign)
  }

  override fun getExtension(): String = actual.extension

  override fun sign(signatory: Signatory?, toSign: InputStream?, destination: OutputStream?) {
    actual.sign(signatory, toSign, destination)
  }
}
