/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.event.player.PlayerJoinEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.interfaces.ModuleInterface

class RecipiesModule : ModuleInterface<PlayerJoinEvent> {
    override fun enabled(): Boolean = Config.RecipiesModule.ENABLED

    override fun on(event: PlayerJoinEvent) {
        val recipies = mutableListOf<NamespacedKey>()
        val iter = Bukkit.recipeIterator()
        while (iter.hasNext()) {
            val recipe = iter.next()
            if (recipe is Keyed) recipies.add(recipe.key)
        }
        event.player.discoverRecipes(recipies)
    }
}
