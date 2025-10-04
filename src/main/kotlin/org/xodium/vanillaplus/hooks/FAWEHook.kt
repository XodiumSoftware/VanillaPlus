package org.xodium.vanillaplus.hooks

import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Utility object for checking plugin availability and handling related dependencies. */
object FAWEHook {
    /**
     * Checks if a specified plugin is available and optionally logs a warning if not found.
     * @return true if the plugin is installed and enabled, false otherwise.
     */
    fun get(): Boolean {
        val plugin = instance.server.pluginManager.getPlugin("WorldEdit") != null
        if (!plugin) {
            val callerClassName = Thread.currentThread().stackTrace[2].className
            val simpleName = callerClassName.substringAfterLast('.')
            instance.logger.warning("FAWE or WorldEdit not found, disabling $simpleName")
        }

        return plugin
    }
}
