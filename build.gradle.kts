plugins {
    id("java")
}

group = "org.xodium.vanillaplus"
version = "1.1.2"
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
    register<Jar>("customJar") {
        dependsOn("jar")
        from(sourceSets.main.get().output)
        doFirst {
            val pluginYmlContent = """
                name: VanillaPlus
                description: ${project.description}
                main: org.xodium.vanillaplus.VanillaPlus
                version: ${project.version}
                api-version: 1.21.3
                authors:
                  - XodiumSoftware
                commands:
                  vanillaplus:
                    description: Main command for VanillaPlus
                    usage: /vanillaplus
                permissions:
                  vanillaplus.doubledoors:
                    description: Allows to open double doors with one click
                    default: true
                  vanillaplus.knock:
                    description: Allows to knock on doors using left-click
                    default: true
                  vanillaplus.autoclose:
                    description: Allows players to have their doors close automatically
                    default: true
            """.trimIndent()
            val pluginYmlFile = file("$buildDir/tmp/plugin.yml")
            pluginYmlFile.writeText(pluginYmlContent)
        }
        from("$buildDir/tmp/plugin.yml") {
            into("META-INF")
        }
    }
}