package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Horse
import org.bukkit.entity.Player
import org.bukkit.entity.WanderingTrader
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a module handling wandering trader mechanics within the system. */
internal class WanderingTraderModule : ModuleInterface<WanderingTraderModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: PlayerInteractEntityEvent) {
        if (!enabled()) return
        val player = event.player
        val entity = event.rightClicked

        if (entity !is WanderingTrader) return

        val horse = findLeashedHorse(player) ?: return
        val emeralds = calculateEmeraldValue(horse)
        if (emeralds <= 0) return

        horse.remove()
        player.inventory.addItem(ItemStack.of(Material.EMERALD, emeralds))
        player.sendActionBar("You traded your horse for".mm())
    }

    private fun findLeashedHorse(player: Player): Horse? =
        player
            .getNearbyEntities(10.0, 10.0, 10.0)
            .filterIsInstance<Horse>()
            .firstOrNull { it.leashHolder == player }

    private fun calculateEmeraldValue(horse: Horse): Int {
        val speed = horse.getAttribute(Attribute.MOVEMENT_SPEED)?.baseValue?.times(10) ?: 0.0
        val jump = horse.jumpStrength * 10
        return ((speed + jump) - 12).toInt().coerceAtLeast(1)
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
