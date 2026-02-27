package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.block.ChiseledBookshelf
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling bookshelf mechanics within the system. */
internal object BookshelfModule : ModuleInterface {
    @EventHandler
    fun on(event: PlayerInteractEvent) {
        (event.clickedBlock?.state as? ChiseledBookshelf)?.inventory?.let(event.player::openInventory)
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
