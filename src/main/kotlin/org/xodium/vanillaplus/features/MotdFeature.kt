package org.xodium.vanillaplus.features

import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.server.ServerListPingEvent
import org.xodium.vanillaplus.interfaces.FeatureInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a feature handling MOTD mechanics within the system. */
internal object MotdFeature : FeatureInterface {
    val config: Config = Config()

    @EventHandler(priority = EventPriority.HIGH)
    fun on(event: ServerListPingEvent) = event.motd(config.motd.joinToString("\n").mm())

    @Serializable
    data class Config(
        val motd: List<String> =
            listOf(
                "<gradient:#CB2D3E:#EF473A><b>Ultimate Private SMP</b></gradient>",
                "<gradient:#FFE259:#FFA751><b>➤ WELCOME BACK LADS!</b></gradient>",
            ),
    )
}
