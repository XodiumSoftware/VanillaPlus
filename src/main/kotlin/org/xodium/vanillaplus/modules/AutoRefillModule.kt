/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface

class AutoRefillModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.AutoRefillModule().enabled

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
        TODO("Implement properly")
        val inv = player.inventory
        val playerSetting = main.getPlayerSetting(player)
        val item = if (offHand) inv.itemInOffHand else inv.itemInMainHand
        val mat = item.type
        val currentSlot = inv.heldItemSlot

        if (item.amount != 1) return
        if (!player.hasPermission(Perms.AutoRefill.USE)) return
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

    val guiItem = ItemBuilder.from(Material.MILK_BUCKET)
        .name(Utils.mangoFormat("AutoRefill").mm())
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to toggle <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Auto refills your items when empty",
                "   <gray>Shortcut: <gold>/autorefill | /ar"
            ).mm()
        )
        .asGuiItem { player, _ -> toggle(player) }

    fun toggle(player: Player) {
        TODO("toggle in database")
    }
}