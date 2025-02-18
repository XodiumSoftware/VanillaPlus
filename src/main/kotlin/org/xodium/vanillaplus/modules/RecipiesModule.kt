/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface

class RecipiesModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.RecipiesModule().enabled

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerJoinEvent) {
        event.player.discoverRecipes(
            Bukkit.recipeIterator().asSequence().filterIsInstance<Keyed>().map { it.key }.toList()
        )
    }
}