/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface

class RefillModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.RefillModule().enabled

    @EventHandler
    fun on(event: PlayerItemConsumeEvent) = attemptRefill(event.getPlayer())

    @EventHandler
    fun on(event: BlockPlaceEvent) = attemptRefill(event.getPlayer())

    @EventHandler
    fun on(event: PlayerInteractEvent) = attemptRefill(event.getPlayer())

    private fun attemptRefill(player: Player) {
        attemptRefill(player, true)
        attemptRefill(player, false)
    }

    private fun attemptRefill(player: Player, offHand: Boolean) {
        if (Utils.isAllowedGameMode(player, main.config.getBoolean("allow-in-adventure-mode"))) return

        val inv = player.inventory
        val playerSetting = main.getPlayerSetting(player)
        val item = if (offHand) inv.itemInOffHand else inv.itemInMainHand
        val mat = item.type
        val currentSlot = inv.heldItemSlot

        if (item.amount != 1) return
        if (!player.hasPermission("besttools.refill")) return
        if (!playerSetting.isRefillEnabled()) {
            if (!playerSetting.isHasSeenRefillMessage()) {
                sendMessage(player, main.messages?.MSG_REFILL_USAGE)
                playerSetting?.setHasSeenRefillMessage()
            }
            return
        }

        val refillSlot = Utils.getMatchingStackPosition(inv, mat, if (offHand) 45 else inv.heldItemSlot)
        if (refillSlot != -1) {
            Utils.refillStack(inv, refillSlot, if (offHand) 40 else currentSlot, inv.getItem(refillSlot))
        }
    }
}