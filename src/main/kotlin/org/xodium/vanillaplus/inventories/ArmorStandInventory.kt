package org.xodium.vanillaplus.inventories

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.ExtUtils.mm

@Suppress("UnstableApiUsage")
internal class ArmorStandInventory(
    private val armorStand: ArmorStand,
) : InventoryHolder {
    companion object {
        private const val INVENTORY_SIZE = 54

        // Slot positions for equipment
        const val HELMET_SLOT = 13
        const val CHESTPLATE_SLOT = 22
        const val LEGGINGS_SLOT = 31
        const val BOOTS_SLOT = 40
        const val MAIN_HAND_SLOT = 21
        const val OFF_HAND_SLOT = 23

        // Slot positions for toggle buttons
        const val NAME_TAG_SLOT = 16
        const val ARMS_SLOT = 25
        const val SIZE_SLOT = 34
        const val BASE_PLATE_SLOT = 43

        // Item names
        private const val EMPTY_SLOT_NAME = ""
        private const val TOGGLE_NAME_TAG_NAME = "Toggle Name Tag"
        private const val TOGGLE_ARMS_NAME = "Toggle Arms"
        private const val TOGGLE_SIZE_NAME = "Toggle Size"
        private const val TOGGLE_BASE_PLATE_NAME = "Toggle Base Plate"
    }

    private val inventory: Inventory =
        instance.server.createInventory(
            this,
            INVENTORY_SIZE,
            armorStand.customName() ?: armorStand.name.mm(),
        )

    init {
        content()
    }

    override fun getInventory(): Inventory = inventory

    private fun content() {
        inventory.fill(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).name(EMPTY_SLOT_NAME))

        inventory.setItem(HELMET_SLOT, armorStand.equipment.helmet)
        inventory.setItem(CHESTPLATE_SLOT, armorStand.equipment.chestplate)
        inventory.setItem(LEGGINGS_SLOT, armorStand.equipment.leggings)
        inventory.setItem(BOOTS_SLOT, armorStand.equipment.boots)
        inventory.setItem(MAIN_HAND_SLOT, armorStand.equipment.itemInMainHand)
        inventory.setItem(OFF_HAND_SLOT, armorStand.equipment.itemInOffHand)

        inventory.setItem(
            NAME_TAG_SLOT,
            createToggleItem(armorStand.isCustomNameVisible, TOGGLE_NAME_TAG_NAME),
        )
        inventory.setItem(
            ARMS_SLOT,
            createToggleItem(armorStand.hasArms(), TOGGLE_ARMS_NAME),
        )
        inventory.setItem(
            SIZE_SLOT,
            createToggleItem(armorStand.isSmall, TOGGLE_SIZE_NAME),
        )
        inventory.setItem(
            BASE_PLATE_SLOT,
            createToggleItem(armorStand.hasBasePlate(), TOGGLE_BASE_PLATE_NAME),
        )
    }

    private fun ItemStack.name(name: String): ItemStack = apply { setData(DataComponentTypes.CUSTOM_NAME, name.mm()) }

    private fun Inventory.fill(item: ItemStack) {
        for (i in 0 until size) setItem(i, item)
    }

    private fun createToggleItem(
        isActive: Boolean,
        name: String,
    ): ItemStack = ItemStack.of(if (isActive) Material.GREEN_WOOL else Material.RED_WOOL).name(name)
}
