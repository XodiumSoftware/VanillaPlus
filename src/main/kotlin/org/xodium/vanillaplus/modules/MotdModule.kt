package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.server.ServerListPingEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents a module handling MOTD mechanics within the system. */
internal object MotdModule : ModuleInterface {
    @EventHandler(priority = EventPriority.HIGH)
    fun on(event: ServerListPingEvent) = motd(event)

    /**
     * Sets the MOTD for the server list ping event.
     * @param event The server list ping event.
     */
    private fun motd(event: ServerListPingEvent) = event.motd(MM.deserialize(config.motdModule.motd.joinToString("\n")))

    @Serializable
    data class Config(
        var enabled: Boolean = true,
        val motd: List<String> =
            listOf(
                "<gradient:#CB2D3E:#EF473A><b>Ultimate Private SMP</b></gradient>",
                "<gradient:#FFE259:#FFA751><b>➤ WELCOME BACK LADS!</b></gradient>",
            ),
    )
}
