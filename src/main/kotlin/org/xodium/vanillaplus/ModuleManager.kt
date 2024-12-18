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
        /**
         * Initializes the modules by performing the following steps:
         * 1. A list of modules to manage is created.
         * 2. Filters the modules to only keep those where the `enabled()` method returns `true`.
         * 3. Registers each enabled module's event listeners with the Bukkit plugin manager.
         * 4. Logs the time taken to load each module for diagnostic purposes.
         */
        listOf(DoorsModule(), SaplingModule())
            .filter { it.enabled() }
            .forEach { mod ->
                val startTime = System.currentTimeMillis()
                instance.server.pluginManager.registerEvents(mod, instance)
                val endTime = System.currentTimeMillis()
                instance.logger.info("Loaded: ${mod.javaClass.simpleName} | Took ${endTime - startTime}ms")
            }
    }
}