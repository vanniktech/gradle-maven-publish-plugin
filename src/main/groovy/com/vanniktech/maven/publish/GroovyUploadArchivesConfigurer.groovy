package com.vanniktech.maven.publish

import org.gradle.api.Project
import org.gradle.api.tasks.Upload
import org.jetbrains.annotations.NotNull

class GroovyUploadArchivesConfigurer extends UploadArchivesConfigurer {

    GroovyUploadArchivesConfigurer(@NotNull Project project, @NotNull MavenPublishPluginExtension extension) {
        super(project, extension)
    }

    @Override
    protected void configureMavenDeployer(@NotNull Upload upload, @NotNull MavenPublishTarget target) {
        MavenPublishPlugin.configureMavenDeployer(project, upload, target)
    }
}
