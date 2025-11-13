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
    }
}

rootProject.name = "PetBreeds"
include(":app")
include(":core:model")
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:data")
include(":core:domain")
include(":core:ui")
include(":core:preferences")
include(":core:resources")
include(":feature")
include(":feature:breeds")
include(":feature:favorites")
include(":feature:details")
include(":feature:onboarding")
include(":feature:splash")
include(":feature:profile")
