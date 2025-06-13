/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.managers

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.PREFIX
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.Utils
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/** Represents the config manager within the system. */
object ConfigManager {
    private val configPath = instance.dataFolder.toPath().resolve("config.json")
    private val objectMapper = jacksonObjectMapper()
        .registerModules(JavaTimeModule())
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    private val permPrefix = "${instance::class.simpleName}.config".lowercase()

    @Volatile
    var data: ConfigData = ConfigData()
        private set

    /**
     * Initializes the configuration by loading settings from the config file.
     * @param silent If true, suppresses logging messages during loading.
     */
    fun load(silent: Boolean = false) {
        try {
            data = ConfigData()
            if (Files.exists(configPath)) {
                if (!silent) instance.logger.info("Config: Loading settings.")
                data = objectMapper
                    .readerForUpdating(data)
                    .readValue(Files.readString(configPath))
                if (!silent) instance.logger.info("Config: Settings loaded successfully.")
            } else {
                instance.logger.info("Config: No config file found, creating new config.")
            }
            save()
        } catch (e: IOException) {
            instance.logger.severe("Config: Failed to load config file: ${e.printStackTrace()}")
        }
    }

    /** Saves the current settings to the config file. */
    private fun save() {
        try {
            instance.logger.info("Config: Saving settings.")
            Files.createDirectories(configPath.parent)
            Files.writeString(
                configPath,
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
            instance.logger.info("Config: Settings saved successfully.")
        } catch (e: IOException) {
            instance.logger.severe("Config: Failed to save config file: ${e.printStackTrace()}")
        }
    }

    /**
     * Creates the command for the configuration GUI.
     * @return A LiteralArgumentBuilder for the "config" command.
     */
    @Suppress("UnstableApiUsage")
    fun cmd(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("reload")
            .requires { it.sender.hasPermission(perm()) }
            .executes { ctx ->
                Utils.tryCatch(ctx) {
                    load(true)
                    instance.logger.info("Config: Reloaded settings.")
                    (it.sender as Player).sendMessage("$PREFIX Reloaded config".mm())
                }
            }
    }

    /**
     * Creates the permission for the configuration reload command.
     * @return A Permission object for the config reload command.
     */
    fun perm(): Permission {
        return Permission(
            "$permPrefix.use",
            "Allows use of the config reload command",
            PermissionDefault.OP
        )
    }
}