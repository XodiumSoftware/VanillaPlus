package org.xodium.illyriaplus.mechanics

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.server.ServerListPingEvent
import org.xodium.illyriaplus.interfaces.MechanicInterface
import org.xodium.illyriaplus.utils.Utils.MM

/** Represents a module handling MOTD mechanics within the system. */
internal object MotdMechanic : MechanicInterface {
    private val MOTD: List<String> =
        listOf(
            "<gradient:#CB2D3E:#EF473A><b>Ultimate Private SMP</b></gradient>",
            "<gradient:#FFE259:#FFA751><b>➤ WELCOME BACK LADS!</b></gradient>",
        )

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: ServerListPingEvent) {
        event.motd(MM.deserialize(MOTD.joinToString("\n")))
    }
}
