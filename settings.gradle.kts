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
    }
}

rootProject.name = "aiAssistant"

include(":android:app")
include(":android:core-model")
include(":android:core-data")
include(":android:core-network")
include(":android:core-ml")
include(":android:feature-import")
include(":android:feature-docs")
include(":android:feature-search")
include(":android:feature-ai")
include(":android:feature-chat")
include(":android:feature-settings")
