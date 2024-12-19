package org.xodium.vanillaplus

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.DoorsModule
import org.xodium.vanillaplus.modules.SaplingModule

/**
 * The `ModuleManager` is responsible for managing and initializing modules in the VanillaPlus plugin.
 * It handles the registration of modules as Bukkit event listeners and ensures only enabled modules
 * are processed during the server startup phase.
 *
 * This object initializes its modules when the server starts and logs the loading time for each
 * enabled module for monitoring performance.
 */
object ModuleManager {
    init {
        listOf(DoorsModule(), SaplingModule())
            .filter { it.enabled() }
            .forEach { mod ->
                // TODO: something is off with the timer.
                val startTime = System.currentTimeMillis()
                instance.server.pluginManager.registerEvents(mod, instance)
                val endTime = System.currentTimeMillis()
                instance.logger.info("Loaded: ${mod.javaClass.simpleName} | Took ${endTime - startTime}ms")
            }
    }
}