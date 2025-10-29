package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.inventories.ArmorStandInventory
import org.xodium.vanillaplus.inventories.ArmorStandInventory.Companion.BOOTS_SLOT
import org.xodium.vanillaplus.inventories.ArmorStandInventory.Companion.CHESTPLATE_SLOT
import org.xodium.vanillaplus.inventories.ArmorStandInventory.Companion.EQUIPMENT_SLOTS
import org.xodium.vanillaplus.inventories.ArmorStandInventory.Companion.HELMET_SLOT
import org.xodium.vanillaplus.inventories.ArmorStandInventory.Companion.LEGGINGS_SLOT

// TODO: fix shift clicking into inventory losing item.

/** Represents a module handling armour stand mechanics within the system. */
internal class ArmorStandModule : ModuleInterface<ArmorStandModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: PlayerInteractAtEntityEvent) {
        if (!enabled() ||
            event.rightClicked !is ArmorStand ||
            event.player.isSneaking ||
            event.player.inventory.itemInMainHand.type == Material.NAME_TAG
        ) {
            return
        }
        event.player.openInventory(ArmorStandInventory(event.rightClicked as ArmorStand).inventory)
        event.isCancelled = true
    }

    @EventHandler
    fun on(event: InventoryClickEvent) {
        val inventory = event.inventory
        val clickedInventory = event.clickedInventory

        if (inventory.holder is ArmorStandInventory && clickedInventory == inventory) {
            val armorStandInventory = inventory.holder as ArmorStandInventory

            if (event.slot in EQUIPMENT_SLOTS) {
                val cursorItem = event.cursor
                if (cursorItem.type != Material.AIR) {
                    if (!isValidItemForSlot(event.slot, cursorItem.type)) {
                        event.isCancelled = true
                        return
                    }
                }
                instance.server.scheduler.runTask(
                    instance,
                    Runnable { armorStandInventory.handleClick(event.slot) },
                )
                return
            }

            event.isCancelled = true
            armorStandInventory.handleClick(event.slot)
        }
    }

    /**
     * Checks if a given item type is valid for a specific equipment slot.
     * @param slot The inventory slot being checked.
     * @param itemType The material of the item to validate.
     * @return `true` if the item is valid for the slot, `false` otherwise.
     */
    private fun isValidItemForSlot(
        slot: Int,
        itemType: Material,
    ): Boolean =
        when (slot) {
            HELMET_SLOT ->
                Tag.ITEMS_HEAD_ARMOR.isTagged(itemType) ||
                    Tag.ITEMS_SKULLS.isTagged(itemType) ||
                    itemType == Material.CARVED_PUMPKIN

            CHESTPLATE_SLOT -> Tag.ITEMS_CHEST_ARMOR.isTagged(itemType) || itemType == Material.ELYTRA
            LEGGINGS_SLOT -> Tag.ITEMS_LEG_ARMOR.isTagged(itemType)
            BOOTS_SLOT -> Tag.ITEMS_FOOT_ARMOR.isTagged(itemType)
            else -> true
        }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
