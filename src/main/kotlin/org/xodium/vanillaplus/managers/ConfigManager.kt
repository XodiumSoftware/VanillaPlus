package org.xodium.vanillaplus.managers

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.DataInterface
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.key

/** Represents the config manager within the system. */
internal object ConfigManager : DataInterface<String, Any> {
    override val dataClass = Any::class
    override val cache = mutableMapOf<String, Any>()
    override val fileName = "config.json"

    /**
     * Updates module configurations by loading from file and applying to modules.
     * @param modules List of modules to update configurations for.
     */
    fun update(modules: List<ModuleInterface<ModuleInterface.Config>>) {
        modules.forEach { module ->
            val key = module.key()
            if (!cache.containsKey(key)) set(key, get(key) ?: module.config)
        }
        if (modules.isNotEmpty()) instance.logger.info("Config updated successfully")
    }
}
