package org.xodium.vanillaplus

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.DoorsModule
import org.xodium.vanillaplus.modules.SaplingModule

object ModuleManager {
    private val VP = instance

    init {
        listOf(DoorsModule(), SaplingModule())
            .onEach { it.config() }
            .filter { it.enabled() }
            .forEach { mod ->
                val startTime = System.currentTimeMillis()
                VP.server.pluginManager.registerEvents(mod, VP)
                val endTime = System.currentTimeMillis()
                VP.logger.info("Loaded: ${mod.javaClass.simpleName} | Took ${endTime - startTime}ms")
            }
    }
}