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
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager

/** Represents a module handling recipe mechanics within the system. */
class RecipiesModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigManager.data.recipiesModule.enabled

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return
        event.player.discoverRecipes(
            instance.server.recipeIterator().asSequence().filterIsInstance<Keyed>().map { it.key }.toList()
        )
    }
}