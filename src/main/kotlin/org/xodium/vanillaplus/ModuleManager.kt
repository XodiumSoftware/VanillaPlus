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
        listOf(
            DoorsModule(),
            SaplingModule()
        ).filter { it.enabled() }
            .forEach {
                val t = System.currentTimeMillis()
                it.init()
                instance.server.pluginManager.registerEvents(it, instance)
                instance.logger.info("Loaded: ${it.javaClass.simpleName} | Took ${System.currentTimeMillis() - t}ms")
            }
    }
}

