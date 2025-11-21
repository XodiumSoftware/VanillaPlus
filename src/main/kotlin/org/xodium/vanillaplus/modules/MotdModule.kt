package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.server.ServerListPingEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a module handling MOTD mechanics within the system. */
internal class MotdModule : ModuleInterface<MotdModule.Config> {
    override val config: Config = Config()

    @EventHandler(priority = EventPriority.HIGH)
    fun on(event: ServerListPingEvent) {
        if (!enabled()) return

        event.motd(config.motd.joinToString("\n").mm())
    }

    data class Config(
        val motd: List<String> =
            listOf(
                "<gradient:#CB2D3E:#EF473A><b>Ultimate Private SMP</b></gradient>",
                "<gradient:#FFE259:#FFA751><b>➤ WELCOME BACK LADS!</b></gradient>",
            ),
    ) : ModuleInterface.Config
}
