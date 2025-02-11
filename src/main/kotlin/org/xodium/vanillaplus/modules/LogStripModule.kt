/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.MaterialRegistry


class LogStripModule : ModuleInterface {
    override fun enabled(): Boolean = Config.LogStripModule.ENABLED

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerInteractEvent) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return
        if (!isAxe(event.getItem())) return
        if (Config.LogStripModule.ALLOW_SHIFT_RIGHT_CLICK_STRIPPING && event.getPlayer().isSneaking) return
        event.isCancelled = true
    }

    private fun isAxe(item: ItemStack?): Boolean =
        item?.let { it.amount > 0 && it.type in MaterialRegistry.AXES } == true
}