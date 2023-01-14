pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        id("com.google.devtools.ksp") version kspVersion apply false
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "service-compiler"

include(":app")
include(":app_lib")
include(":app_module_a")
include(":app_module_b")
include(":lib")
