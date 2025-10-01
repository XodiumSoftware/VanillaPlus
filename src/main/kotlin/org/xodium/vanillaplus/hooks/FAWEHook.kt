package org.xodium.vanillaplus.hooks

import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Utility object for checking plugin availability and handling related dependencies. */
object FAWEHook {
    /**
     * Checks if a specified plugin is available and optionally logs a warning if not found.
     * @param module The name of the module that will be disabled.
     * @return true if the plugin is installed and enabled, false otherwise.
     */
    fun get(module: String): Boolean {
        val plugin = instance.server.pluginManager.getPlugin("WorldEdit") != null
        if (!plugin) instance.logger.warning("FAWE or WorldEdit not found, disabling $module")

        return plugin
    }
}
