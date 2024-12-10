plugins {
    id("java")
}

group = "org.xodium.vanillaplus"
version = "1.2.0"
description = "Minecraft plugin that enhances the base gameplay."

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("net.kyori:adventure-api:4.17.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    jar {
        manifest {
            attributes(mapOf("paperweight-mappings-namespace" to "mojang"))
        }
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