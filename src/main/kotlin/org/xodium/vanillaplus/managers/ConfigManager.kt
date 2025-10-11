package org.xodium.vanillaplus.managers

import org.xodium.vanillaplus.interfaces.DataInterface
import org.xodium.vanillaplus.interfaces.ModuleInterface

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
    fun saveConfig(data: Map<String, ModuleInterface.Config>) = setAll(data)
}
