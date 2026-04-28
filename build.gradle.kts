// Root project - aggregates IllyriaCore and IllyriaKingdoms
// Defines common versions and settings for all subprojects

plugins {
    id("java")
    id("idea")
}

val buildNumber =
    providers
        .exec { commandLine("git", "rev-list", "--count", "HEAD") }
        .standardOutput.asText
        .map { it.trim() }

ext {
    set("mcVersion", "1.21.11")
    set("kotlinVersion", "2.3.21")
    set("buildNumber", buildNumber.get())
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}
