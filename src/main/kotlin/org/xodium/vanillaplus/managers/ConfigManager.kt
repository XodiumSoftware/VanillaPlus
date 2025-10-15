package org.xodium.vanillaplus.managers

import com.fasterxml.jackson.core.JsonProcessingException
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
            val newData =
                get(key)?.let { data ->
                    try {
                        jsonMapper.updateValue(
                            jsonMapper.convertValue(data, module.config::class.java),
                            module.config,
                        )
                    } catch (e: JsonProcessingException) {
                        instance.logger.warning(
                            "Failed to parse config for ${module::class.simpleName}. Using defaults. Error: ${e.message}",
                        )
                        module.config
                    }
                } ?: module.config

            set(key, newData)
        }
        if (modules.isNotEmpty()) instance.logger.info("Config updated successfully")
    }
}
