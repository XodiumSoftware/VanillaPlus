package org.xodium.vanillaplus.modules

import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt

/** Represents a module handling wandering trader mechanics within the system. */
internal class WanderingTraderModule : ModuleInterface<WanderingTraderModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: CreatureSpawnEvent) {
        if (!config.enabled || event.entityType != EntityType.WANDERING_TRADER) return

        val world = event.location.world ?: return
        val location = event.location

        for (player in world.players) {
            if (player.location.distanceSquared(location) <= config.notifyRadius * config.notifyRadius) {
                player.sendActionBar(config.traderMessage.mm())
            }
        }
    }

    data class Config(
        override var enabled: Boolean = true,
        var notifyRadius: Double = 32.0,
        var traderMessage: String = "A Wandering Trader has arrived nearby!".mangoFmt()
    ) : ModuleInterface.Config
}
