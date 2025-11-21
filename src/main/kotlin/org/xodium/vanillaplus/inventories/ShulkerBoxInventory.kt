package org.xodium.vanillaplus.inventories

import org.bukkit.block.ShulkerBox
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a class handling shulker box inventory implementation within the system. */
internal object ShulkerBoxInventory {
    private const val SHULKER_BOX_SIZE = 27

    val ShulkerBox.inventory: Inventory
        get() =
            instance.server.createInventory(this, SHULKER_BOX_SIZE, customName() ?: "".mm()).apply {
                contents = inventory.contents
            }

    /**
     * Handles the event when a player shift-right-clicks a shulker box in their inventory to open it.
     * @param event The inventory click event triggered by the player action.
     */
    fun shulker(event: InventoryClickEvent) {
        if (event.click != ClickType.SHIFT_RIGHT ||
            event.clickedInventory?.type != InventoryType.PLAYER ||
            event.currentItem !is ShulkerBox
        ) {
            return
        }

        event.isCancelled = true

        instance.server.scheduler.runTask(
            instance,
            Runnable { event.whoClicked.openInventory((event.currentItem as ShulkerBox).inventory) },
        )
    }
}
