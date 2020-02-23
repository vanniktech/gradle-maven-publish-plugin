package com.vanniktech.maven.publish.tasks

import org.gradle.jvm.tasks.Jar

@Suppress("UnstableApiUsage")
open class EmptyJavadocsJar : Jar() {

    init {
        archiveClassifier.set("javadoc")
    }
}
