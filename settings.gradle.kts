pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Jexoplayer"
include(":app")
include(":jexoplayer")

include(":jexoplayer:ui:tv")
include(":jexoplayer:ui:mobile")
//include(":tv")
