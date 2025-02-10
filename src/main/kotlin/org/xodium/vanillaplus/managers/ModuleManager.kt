/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.managers

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.DoorsModule
import org.xodium.vanillaplus.modules.GuiModule
import org.xodium.vanillaplus.modules.MotdModule
import org.xodium.vanillaplus.modules.TreesModule
import kotlin.time.measureTime

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
            GuiModule(),
            MotdModule(),
            TreesModule(),
        ).filter { it.enabled() }
            .forEach {
                instance.logger.info(
                    "Loaded: ${it.javaClass.simpleName} | Took ${
                        measureTime {
                            it.init()
                            instance.server.pluginManager.registerEvents(it, instance)
                        }.inWholeMilliseconds
                    }ms"
                )
            }
    }
}
