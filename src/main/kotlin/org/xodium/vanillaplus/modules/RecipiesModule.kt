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

/** Represents a module handling recipe mechanics within the system. */
class RecipiesModule : ModuleInterface<RecipiesModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return
        event.player.discoverRecipes(
            instance.server.recipeIterator().asSequence().filterIsInstance<Keyed>().map { it.key }.toList()
        )
    }

    data class Config(
        override val enabled: Boolean = true
    ) : ModuleInterface.Config
}