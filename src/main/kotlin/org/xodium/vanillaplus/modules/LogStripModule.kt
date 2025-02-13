/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.MaterialRegistry


class LogStripModule : ModuleInterface<PlayerInteractEvent> {
    override fun enabled(): Boolean = Config.LogStripModule.ENABLED

    override fun on(event: PlayerInteractEvent) {
        when {
            event.action != Action.RIGHT_CLICK_BLOCK -> return
            event.clickedBlock?.type !in MaterialRegistry.LOGS -> return
            !isAxe(event.item) -> return
            Config.LogStripModule.ALLOW_SHIFT_RIGHT_CLICK_STRIPPING && event.player.isSneaking -> return
            else -> event.isCancelled = true
        }
    }

    private fun isAxe(item: ItemStack?): Boolean =
        item?.let { it.amount > 0 && it.type in MaterialRegistry.AXES } == true
}