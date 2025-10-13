package org.xodium.vanillaplus.managers

import com.fasterxml.jackson.core.JsonProcessingException
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.DataInterface
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.key
import kotlin.io.path.exists
import kotlin.io.path.readText

/** Represents the config manager within the system. */
internal object ConfigManager : DataInterface<String, ModuleInterface.Config> {
    override val dataClass = ModuleInterface.Config::class
    override val cache = mutableMapOf<String, ModuleInterface.Config>()
    override val fileName = "config.json"

    /**
     * Loads all configuration settings from the config file.
     * @return A map of module names to their respective configuration objects.
     *         Returns an empty map if no configuration exists or if an error occurs during loading.
     */
    fun loadConfig(): Map<String, ModuleInterface.Config> = getAll()

    /**
     * Saves the provided configuration data to the config file.
     * @param data A map of module names to their respective configuration objects to be saved.
     */
    fun saveConfig(data: Map<String, ModuleInterface.Config>) = setAll(LinkedHashMap(data))

    /**
     * Updates module configurations by loading from file and applying to modules.
     * @param modules List of modules to update configurations for.
     */
    fun updateConfig(modules: List<ModuleInterface<ModuleInterface.Config>>) {
        val allConfigs = loadConfig()
        when {
            allConfigs.isNotEmpty() -> instance.logger.info("Config: Loaded successfully")
            !filePath.exists() -> instance.logger.info("Config: No config file found, a new one will be created")
            else -> {
                instance.logger.warning("Config: Failed to load, using defaults")
                try {
                    instance.logger.warning("Config file content: ${filePath.readText()}")
                } catch (e: Exception) {
                    instance.logger.warning("Config: Failed to read config file: ${e.message}")
                    e.printStackTrace()
                }
            }
        }

        modules.forEach { module ->
            val configKey = module.key()
            allConfigs[configKey]?.let { configData ->
                try {
                    jsonMapper
                        .readerForUpdating(module.config)
                        .readValue(jsonMapper.writeValueAsString(configData))
                } catch (e: JsonProcessingException) {
                    instance.logger.warning(
                        "Failed to parse config for ${module::class.simpleName}. Using defaults. Error: ${e.message}",
                    )
                }
            }
            cache[configKey] = module.config
        }

        saveConfig(cache)
    }
}
