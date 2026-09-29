pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal() // core-hackerai published via ./gradlew :core-hackerai:publishToMavenLocal
    }
}

rootProject.name = "hackerai-companion"
include(":app")
