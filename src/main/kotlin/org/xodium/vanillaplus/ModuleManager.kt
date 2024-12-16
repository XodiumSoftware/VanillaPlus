package org.xodium.vanillaplus

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.modules.DoorsModule
import org.xodium.vanillaplus.modules.SaplingModule
import java.util.stream.Stream

object ModuleManager {
    private val VP = instance

    init {
        Stream.of<ModuleInterface?>(DoorsModule(), SaplingModule())
            .peek { obj: ModuleInterface? -> obj!!.config() }
            .filter { obj: ModuleInterface? -> obj!!.enabled() }
            .forEach { mod: ModuleInterface? ->
                val startTime = System.currentTimeMillis()
                VP.server.pluginManager.registerEvents(mod!!, VP)
                val endTime = System.currentTimeMillis()
                VP.logger
                    .info(
                        ("Loaded: " + mod.javaClass.getSimpleName() + "| Took " + (endTime - startTime)
                                + "ms")
                    )
            }
    }
}