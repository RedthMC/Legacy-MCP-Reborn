plugins {
    id("net.minecraftforge.gradle.mcp")
}

val minecraft_version: String by project
val mcp_version: String by project
val mappings_channel: String by project
val mappings_version: String by project
val spi_version: String by project

mcp {
    setConfig("$minecraft_version-$mcp_version")
    pipeline.set("joined")
}
