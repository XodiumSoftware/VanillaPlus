package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.block.ChiseledBookshelf
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ChiseledBookshelfInventory
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling bookshelf mechanics within the system. */
internal object BookshelfModule : ModuleInterface {
    @EventHandler
    fun on(event: PlayerInteractEvent) {
        if (!event.player.isSneaking) return

        (event.clickedBlock?.state as? ChiseledBookshelf)?.snapshotInventory?.let {
            event.isCancelled = true
            event.player.openInventory(it)
        }
    }

    @EventHandler
    fun on(event: InventoryClickEvent) {
        if (event.inventory is ChiseledBookshelfInventory) event.isCancelled = true
    }

    @EventHandler
    fun on(event: InventoryDragEvent) {
        if (event.inventory is ChiseledBookshelfInventory) event.isCancelled = true
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
