package org.xodium.vanillaplus.hooks

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.features.TreesFeature

/** A utility object for checking plugin availability and handling related dependencies. */
object WorldEditHook {
    /**
     * Checks if a specified plugin is available and optionally logs a warning if not found.
     * @return true if the plugin is installed and enabled, false otherwise.
     */
    fun get(): Boolean {
        val plugin = instance.server.pluginManager.getPlugin("WorldEdit") != null

        if (!plugin) {
            instance.logger.warning("FAWE or WorldEdit not found, disabling ${TreesFeature.javaClass.simpleName}")
        }

        return plugin
    }
}
