package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.Keyed
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling recipe mechanics within the system. */
internal class RecipiesModule : ModuleInterface {
    val config: Config = Config()

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        if (!config.enabled) return

        event.player.discoverRecipes(
            instance.server
                .recipeIterator()
                .asSequence()
                .filterIsInstance<Keyed>()
                .map { it.key }
                .toList(),
        )
    }

    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
