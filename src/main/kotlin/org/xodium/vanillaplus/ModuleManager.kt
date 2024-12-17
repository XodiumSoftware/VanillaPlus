package org.xodium.vanillaplus

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.DoorsModule
import org.xodium.vanillaplus.modules.SaplingModule

object ModuleManager {
    init {
        listOf(DoorsModule(), SaplingModule())
            .onEach { it.config() }
            .filter { it.enabled() }
            .forEach { mod ->
                val startTime = System.currentTimeMillis()
                instance.server.pluginManager.registerEvents(mod, instance)
                val endTime = System.currentTimeMillis()
                instance.logger.info("Loaded: ${mod.javaClass.simpleName} | Took ${endTime - startTime}ms")
            }
    }
}