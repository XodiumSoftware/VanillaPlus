package org.xodium.vanillaplus.hooks

import de.oliver.fancyholograms.api.FancyHologramsPlugin
import de.oliver.fancyholograms.api.HologramManager
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Utility object for checking plugin availability and handling related dependencies. */
object FancyHologramsHook {
    val manager: HologramManager = FancyHologramsPlugin.get().hologramManager

    /**
     * Checks if a specified plugin is available.
     * @return true if the plugin is installed and enabled, false otherwise.
     */
    fun enabled(): Boolean = instance.server.pluginManager.getPlugin("FancyHolograms") != null
}
