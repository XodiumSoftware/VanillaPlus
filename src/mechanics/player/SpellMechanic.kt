@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus.mechanics.player

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.xodium.illyriaplus.interfaces.MechanicInterface
import org.xodium.illyriaplus.managers.SpellManager

/** Handles spell wand interactions and selection. */
internal object SpellMechanic : MechanicInterface {
    @EventHandler
    fun on(event: PlayerInteractEvent) = SpellManager.handleWandInteraction(event)

    @EventHandler
    fun on(event: PlayerItemHeldEvent) = SpellManager.handleWandSelection(event)
}
