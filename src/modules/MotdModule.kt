package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.server.ServerListPingEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents a module handling MOTD mechanics within the system. */
internal object MotdModule : ModuleInterface {
    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: ServerListPingEvent) {
        event.motd(MM.deserialize(Config.motd.joinToString("\n")))
    }

    /** Represents the config of the module. */
    object Config {
        val motd: List<String> =
            listOf(
                "<gradient:#CB2D3E:#EF473A><b>Ultimate Private SMP</b></gradient>",
                "<gradient:#FFE259:#FFA751><b>➤ WELCOME BACK LADS!</b></gradient>",
            )
    }
}
