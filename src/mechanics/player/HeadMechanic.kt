@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus.mechanics.player

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import org.xodium.illyriaplus.Utils.PlayerUtils.head
import org.xodium.illyriaplus.interfaces.MechanicInterface
import kotlin.random.Random

/** Represents a mechanic handling player head drops within the system. */
internal object HeadMechanic : MechanicInterface {
    const val SKULL_DROP_CHANCE: Double = 0.01

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerDeathEvent) {
        dropPlayerHead(event.player)
    }

    /**
     * Attempts to drop the specified player's head at their current location.
     *
     * @param player The player whose head may be dropped.
     */
    private fun dropPlayerHead(player: Player) {
        if (Random.nextDouble() > SKULL_DROP_CHANCE) return

        player.world.dropItemNaturally(player.location, player.head())
    }
}
