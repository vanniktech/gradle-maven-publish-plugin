package com.vanniktech.maven.publish.tasks

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

public class WorkaroundSignatureType(
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
