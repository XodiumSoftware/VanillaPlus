/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.managers

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.PREFIX
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.Utils
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/** Represents the config manager within the system. */
object ConfigManager {
    private val configPath = instance.dataFolder.toPath().resolve("config.json")
    private val objectMapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
        registerModule(Jdk8Module())
    }
    var data: ConfigData = ConfigData()

    /**
     * Initializes the configuration by loading settings from the config file.
     * @param silent If true, suppresses logging messages during loading.
     */
    fun load(silent: Boolean = false) {
        if (Files.exists(configPath)) {
            if (!silent) instance.logger.info("Config: Loading settings.")
            data = objectMapper.readValue(Files.readString(configPath))
            if (!silent) instance.logger.info("Config: Settings loaded successfully.")
        } else {
            instance.logger.info("Config: No config file found, creating new config.")
            save()
        }
    }

    /** Saves the current settings to the config file. */
    private fun save() {
        instance.logger.info("Config: Saving settings.")
        Files.createDirectories(configPath.parent)
        Files.writeString(
            configPath,
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
        instance.logger.info("Config: Settings saved successfully.")
    }

    /**
     * Creates the command for the configuration GUI.
     * @return A LiteralArgumentBuilder for the "config" command.
     */
    @Suppress("UnstableApiUsage")
    fun cmd(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("reload")
            .requires { it.sender.hasPermission(Perms.Config.RELOAD) }
            .executes { it ->
                Utils.tryCatch(it) {
                    load(true)
                    instance.logger.info("Config: Reloaded settings.")
                    (it.sender as Player).sendMessage("$PREFIX Reloaded config".mm())
                }
            }
    }
}