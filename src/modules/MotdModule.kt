package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.server.ServerListPingEvent
import org.xodium.vanillaplus.interfaces.ModuleConfigInterface
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.configDelegate

/** Represents a module handling MOTD mechanics within the system. */
internal object MotdModule : ModuleInterface {
    override val config by configDelegate { Config() }

    @EventHandler(priority = EventPriority.HIGH)
    fun on(event: ServerListPingEvent) {
        event.motd(MM.deserialize(config.motd.joinToString("\n")))
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        override var enabled: Boolean = false,
        val motd: List<String> =
            listOf(
                "<gradient:#CB2D3E:#EF473A><b>Ultimate Private SMP</b></gradient>",
                "<gradient:#FFE259:#FFA751><b>➤ WELCOME BACK LADS!</b></gradient>",
            ),
    ) : ModuleConfigInterface
}
