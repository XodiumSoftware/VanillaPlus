package org.xodium.vanillaplus.managers

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.DataInterface
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.key

/** Manages module configs on disk and in-memory. */
internal object ConfigManager : DataInterface<String, ModuleInterface.Config> {
    override val cache: MutableMap<String, ModuleInterface.Config> = mutableMapOf()
    override val fileName: String = "config.json"

    /**
     * Updates module configurations by merging existing file values into
     * in-code defaults. File/user values take precedence over defaults.
     * @param modules list of modules to update.
     */
    fun update(modules: List<ModuleInterface<ModuleInterface.Config>>) {
        modules.forEach { module ->
            val key = module.key
            val fileConfig = readFileConfig(key, module)
            val mergedConfig = fileConfig?.let { jsonMapper.updateValue(module.config, it) } ?: module.config
            set(key, mergedConfig)
        }
        if (modules.isNotEmpty()) instance.logger.info("Config updated successfully")
    }

    /**
     * Reads a module's configuration from the JSON config file.
     * @param key The unique identifier for the module used in the config file.
     * @param module The module instance used to determine the correct configuration type.
     * @return The parsed configuration object from the file, or `null` if not found, or an error occurred.
     * @see ConfigManager.update
     */
    private fun readFileConfig(
        key: String,
        module: ModuleInterface<ModuleInterface.Config>,
    ): ModuleInterface.Config? =
        try {
            if (filePath.toFile().exists()) {
                jsonMapper.readTree(filePath.toFile())?.get(key)?.let { node ->
                    jsonMapper.treeToValue(node, module.config::class.java)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            instance.logger.warning("Failed to read config section for $key: ${e.message}")
            null
        }
}
