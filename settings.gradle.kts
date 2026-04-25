pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io") {
            content {
                includeGroup("com.github.TeamNewPipe")
            }
        }
    }
}

rootProject.name = "OpuxTube"

include(":app")
include(":core:domain")
include(":core:network")
include(":core:data")
include(":core:database")
include(":core:player")
include(":core:ui")
include(":feature:home")
include(":feature:search")
include(":feature:player")
include(":feature:library")
include(":feature:channel")
