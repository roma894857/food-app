// Root project settings
rootProject.name = "FoodApp"

// Configure repositories for the whole project
repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

// Include the app module
subprojects {
    apply plugin: "org.jetbrains.kotlin.android"
}
