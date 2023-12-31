import net.minecraftforge.gradle.common.util.Utils
import net.minecraftforge.gradle.common.util.VersionJson
import org.jetbrains.gradle.ext.*
import java.util.Locale


plugins {
    id("eclipse")
    id("net.minecraftforge.gradle.mcp") version "6.0.18" apply false
    id("net.minecraftforge.gradle.patcher") version "6.0.18"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
    java
}

println(
    " Java: ${System.getProperty("java.version")} JVM: ${System.getProperty("java.vm.version")}(${System.getProperty("java.vendor")}) Arch: ${System.getProperty("os.arch")}"
)

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val shade: Configuration by configurations.creating {
    configurations.compileClasspath.get().extendsFrom(this)
}

group = "me.yourname"
version = "1.0.0"
val minecraft_version: String by project
val mcp_version: String by project
val mappings_channel: String by project
val mappings_version: String by project
val spi_version: String by project

evaluationDependsOn(":mcp")

repositories {
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

dependencies {
    implementation("net.minecraftforge:forgespi:$spi_version")
    runtimeOnly("me.djtheredstoner:DevAuth-forge-latest:1.1.2")

//    Use the shade to add the lib to the jar
//    or use compile if you want to load the lib from the version.json
//    from a maven repo
//    shade "package-here"
//    compile "package-here"
}

patcher {
    parent.set(project(":mcp"))
    patchedSrc.set(file("src/main/java"))
    mappings(
        mapOf(
            "channel" to mappings_channel,
            "version" to mappings_version,
        )
    )
    mcVersion.set(minecraft_version)
}

var assetsFolder = file("run/assets")

tasks {
    jar {
        for (dep in shade.dependencies) {
            from(project.zipTree(dep)) {
                exclude("META-INF", "META-INF/**")
            }
        }
    }
    register("runclient", JavaExec::class) {
        group = "MCP"
        description = "Runs the client"
        classpath(sourceSets.main.get().runtimeClasspath)
        if (System.getProperty("os.name").lowercase(Locale.ROOT).contains("mac")) {
            jvmArgs("-XstartOnFirstThread")
        }

        val runFolder = file("run")
        val versionJson = Utils.loadJson(downloadAssets.get().meta.get().asFile, VersionJson::class.java)
        workingDir(runFolder)
        args("--gameDir", ".")
        args("--version", minecraft_version)
        args("--assetsDir", runFolder.resolve(assetsFolder))
        args("--assetIndex", versionJson.assetIndex.id)
        args("--accessToken", "0")
        mainClass.set("net.minecraft.client.main.Main")
//    main("net.minecraft.client.main.Main")
    }
    register("setup") {
        group = "MCP"
        description = "Setups the dev workspace"
        dependsOn(":extractMapped")
        mkdir(assetsFolder)
        copy {
            from(downloadAssets.get().output.path)
            into(assetsFolder)
        }
    }
    register("copyAssets") {
        group = "MCP"
        description = "Download and place the assets into the run folder"
        dependsOn(":downloadAssets")
        mkdir("run/assets")
        copy {
            from(downloadAssets.get().output.path)
            into(assetsFolder)
        }
    }
    register("runserver", JavaExec::class.java) {
        mkdir("runserver")
        group = "MCP"
        description = "Runs the server"
        standardInput = System.`in`
        classpath(sourceSets.main.get().runtimeClasspath)
        mainClass.set("net.minecraft.server.Main")
        workingDir("runserver")
    }
}
idea.project.settings {
    runConfigurations {
        register("Minecraft", org.jetbrains.gradle.ext.Application::class) {
            mainClass = "mcp.client.Start"
            workingDirectory = "$projectDir/run"
            moduleName = idea.module.name + ".main"
        }
    }
}
