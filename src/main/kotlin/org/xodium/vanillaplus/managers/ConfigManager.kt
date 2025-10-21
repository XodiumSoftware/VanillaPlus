package org.xodium.vanillaplus.managers

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.DataInterface
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.key
import kotlin.reflect.KClass

/** Manages module configs on disk and in-memory. */
internal object ConfigManager : DataInterface<String, ModuleInterface.Config> {
    override val dataClass: KClass<ModuleInterface.Config> = ModuleInterface.Config::class
    override val cache: MutableMap<String, ModuleInterface.Config> = mutableMapOf()
    override val fileName: String = "config.json"

    /**
     * Updates module configurations by merging existing file values into
     * in-code defaults. File/user values take precedence over defaults.
     * @param modules list of modules to update.
     */
    fun update(modules: List<ModuleInterface<ModuleInterface.Config>>) {
        modules.forEach { module ->
            val key = module.key()

            val fileConfig: ModuleInterface.Config? =
                try {
                    if (filePath.toFile().exists()) {
                        val tree = jsonMapper.readTree(filePath.toFile())
                        val node = tree?.get(key)
                        if (node != null) jsonMapper.treeToValue(node, module.config::class.java) else null
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    instance.logger.warning("Failed to read config section for $key: ${e.message}")
                    null
                }

            val mergedConfig =
                if (fileConfig != null) {
                    try {
                        jsonMapper.updateValue(module.config, fileConfig)
                    } catch (e: Exception) {
                        instance.logger.warning("Failed to merge config for $key: ${e.message}")
                        module.config
                    }
                } else {
                    module.config
                }

            set(key, mergedConfig)
        }

        if (modules.isNotEmpty()) instance.logger.info("Config updated successfully")
    }
}
