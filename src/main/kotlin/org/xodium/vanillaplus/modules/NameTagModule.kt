package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface


class NameTagModule : ModuleInterface<NameTagModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    @EventHandler
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return
        nametag()
    }

    private fun nametag(): Unit = TODO()

    data class Config(
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}