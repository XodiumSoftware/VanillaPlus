/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.hooks.ChestSortHook
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.InventoryTypeRegistry

/** Represents a module handling chest sorting mechanics within the system. */
class ChestSortModule : ModuleInterface {
    override fun enabled(): Boolean = Config.ChestSortModule.ENABLED

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: InventoryClickEvent) {
        //TODO: add a check for specific way of clicking (e.g., shift-click, right-click)
        val inventory = event.inventory
        if (inventory.type in InventoryTypeRegistry.CONTAINERS) ChestSortHook.sort(inventory)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: InventoryCloseEvent) {
        val inventory = event.inventory
        if (inventory.type in InventoryTypeRegistry.CONTAINERS) ChestSortHook.sort(inventory)
    }
}