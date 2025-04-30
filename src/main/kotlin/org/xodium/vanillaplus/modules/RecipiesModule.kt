/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.Keyed
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface

/**
 * Handles functionality related to the Recipes module
 *
 * This module automatically unlocks all available recipes for players when they join the server
 */
class RecipiesModule : ModuleInterface {
    override fun enabled(): Boolean = Config.RecipiesModule.ENABLED

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerJoinEvent) {
        event.player.discoverRecipes(
            instance.server.recipeIterator().asSequence().filterIsInstance<Keyed>().map { it.key }.toList()
        )
    }
}