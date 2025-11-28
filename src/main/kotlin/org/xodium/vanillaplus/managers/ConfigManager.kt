package org.xodium.vanillaplus.managers

import kotlinx.serialization.json.Json
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ConfigData
import java.io.File
import kotlin.time.measureTime

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
     * @return The loaded configuration data.
     */
    fun load(fileName: String = "config.json"): ConfigData {
        val file = File(instance.dataFolder, fileName)

        if (!instance.dataFolder.exists()) instance.dataFolder.mkdirs()

        var config: ConfigData

        val time =
            measureTime {
                config =
                    if (file.exists()) {
                        json.decodeFromString(ConfigData.serializer(), file.readText())
                    } else {
                        ConfigData()
                    }
                file.writeText(json.encodeToString(ConfigData.serializer(), config))
            }

        instance.logger.info(
            "${if (file.exists()) "Loaded configuration from $fileName" else "Created default $fileName"} | Took ${time.inWholeMilliseconds}ms",
        )

        return config
    }
}
