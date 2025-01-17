/*
 * Copyright (c) 2025. Xodium.
 * All rights reserved.
 */

package org.xodium.vanillaplus.managers

import org.bukkit.configuration.file.YamlConfiguration
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.io.InputStreamReader

// FIX: doesnt insert key.
object ConfigManager {
    init {
        val config = instance.config
        val logger = instance.logger
        val defaultConfigStream = instance.getResource("config.yml")
        if (defaultConfigStream != null) {
            val defaultConfig = YamlConfiguration.loadConfiguration(InputStreamReader(defaultConfigStream))
            var isUpdated = false

            for (key in defaultConfig.getKeys(true)) {
                if (!config.contains(key)) {
                    config.set(key, defaultConfig.get(key))
                    logger.info("Added missing config key: '$key' with default value: '${defaultConfig.get(key)}'")
                    isUpdated = true
                }
            }

            if (isUpdated) {
                instance.saveConfig()
                logger.info("Configuration file updated with missing keys.")
            }
        } else {
            logger.warning("Default config file not found in resources.")
        }
    }
}
