package org.xodium.vanillaplus.interfaces

import kotlinx.serialization.json.Json
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.io.File

/** Represents a contract for data within the system. */
internal interface DataInterface {
    companion object {
        private const val CONFIG_FILE = "config.json"
    }

    val json: Json
        get() = Json { prettyPrint = true }

    /** Loads configuration from JSON file. */
    fun load() {
        val config = File(instance.dataFolder, CONFIG_FILE)
        if (!config.exists()) {
            instance.dataFolder.mkdirs()
            save()
            instance.logger.info("Created new config file.")
        } else {
            Json.decodeFromString(getSerializer(), config.readText())
            instance.logger.info("Loaded configs from file.")
        }
    }

    /** Saves configuration to JSON file. */
    fun save() {
        try {
            File(instance.dataFolder, CONFIG_FILE).writeText(json.encodeToString(getSerializer(), TODO()))
            instance.logger.info("Saved configs")
        } catch (e: Exception) {
            instance.logger.warning("Failed to save configs: ${e.message}")
        }
    }
}
