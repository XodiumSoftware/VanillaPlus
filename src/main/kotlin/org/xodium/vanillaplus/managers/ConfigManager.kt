package org.xodium.vanillaplus.managers

import kotlinx.serialization.json.Json
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ConfigData
import java.io.File

/** Manages loading and saving the configuration file. */
internal object ConfigManager {
    private val json =
        Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

    /**
     * Loads or creates the configuration file.
     * @param fileName The name of the configuration file.
     */
    fun load(fileName: String = "config.json") {
        if (!instance.dataFolder.exists()) instance.dataFolder.mkdirs()

        val file = File(instance.dataFolder, fileName)
        val exists = file.exists()

        file.writeText(
            json.encodeToString(
                ConfigData.serializer(),
                if (exists) json.decodeFromString(ConfigData.serializer(), file.readText()) else ConfigData(),
            ),
        )

        instance.logger.info(if (exists) "Loaded configuration from $fileName" else "Created default $fileName")
    }
}
