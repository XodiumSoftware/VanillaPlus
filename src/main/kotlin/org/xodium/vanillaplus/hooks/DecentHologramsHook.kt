package org.xodium.vanillaplus.hooks

import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Utility object for checking plugin availability and handling related dependencies. */
object DecentHologramsHook {
    /**
     * Checks if a specified plugin is available.
     * @return true if the plugin is installed and enabled, false otherwise.
     */
    fun enabled(): Boolean = instance.server.pluginManager.getPlugin("DecentHolograms") != null
}
