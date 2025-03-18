/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.Keyed
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface

/**
 * Module for unlocking all recipes for players when they join the server.
 * When enabled, it allows players to craft any item in the game without needing to unlock the recipe first.
 */
class RecipiesModule : ModuleInterface {
    /**
     * Returns true if the module is enabled in the plugin's configuration.
     */
    override fun enabled(): Boolean = ConfigData.RecipiesModule().enabled

    /**
     * Event handler for the PlayerJoinEvent.
     * When the event is triggered, it unlocks all recipes for the player.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerJoinEvent) {
        event.player.discoverRecipes(
            instance.server.recipeIterator().asSequence().filterIsInstance<Keyed>().map { it.key }.toList()
        )
    }
}