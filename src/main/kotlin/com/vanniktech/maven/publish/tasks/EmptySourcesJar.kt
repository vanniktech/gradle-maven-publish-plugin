package com.vanniktech.maven.publish.tasks

import org.gradle.jvm.tasks.Jar

@Suppress("UnstableApiUsage")
open class EmptySourcesJar : Jar() {

    init {
        archiveClassifier.set("sources")
    }
}
