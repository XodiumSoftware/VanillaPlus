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
    set("mcVersion", "26.1.2")
    set("javaVersion", "25")
    set("buildNumber", buildNumber.get())
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}
