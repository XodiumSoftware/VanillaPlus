@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus.mechanics.player

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.interfaces.MechanicInterface

/** Allows players to open their ender chest by right-clicking with an ender chest in offhand. */
internal object EnderchestMechanic : MechanicInterface {
    @EventHandler
    fun on(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR) return
        if (event.item?.type != Material.ENDER_CHEST) return
        if (event.player.gameMode != GameMode.SURVIVAL) return

        event.isCancelled = true
        instance.server.scheduler.runTask(
            instance,
            Runnable { event.player.openInventory(event.player.enderChest) },
        )
    }
}
