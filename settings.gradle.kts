pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.minecraftforge.net")
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "mcp-reborn"
rootProject.buildFileName = "build.gradle.kts"

include(":mcp")
project(":mcp").apply {
    projectDir = file("projects/mcp")
    buildFileName = "../../mcp.gradle.kts"
}
