plugins {
    id("java")
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

group = "org.xodium.vanillaplus"
version = "1.2.1"
description = "Minecraft plugin that enhances the base gameplay."

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    implementation("net.kyori:adventure-api:4.17.0")
    implementation(kotlin("stdlib-jdk8"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("kotlin", "org.xodium.vanillaplus.kotlin")
    }
    jar {
        enabled = false
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    register("printVersion") {
        doLast {
            println(project.version)
        }
    }
}