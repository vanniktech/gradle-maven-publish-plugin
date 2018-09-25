package com.vanniktech.maven.publish

class MavenPublishRepositories {

    Map<String, MavenPublishRepositoryModel> map = new HashMap<>()

    def propertyMissing(String name, value) {
        // Create the new configuration and add to the map
        def data = new MavenPublishRepositoryModel()
        map.put(name, data)

        // setup and execute the client closure to configure the repo
        def closure = value as Closure
        closure.delegate = data
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.run()
    }
}