package org.xodium.vanillaplus.inventories

import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.ExtUtils.fill
import org.xodium.vanillaplus.utils.ExtUtils.lore
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.name
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt

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

        // Slot positions for the extra options button
        const val EXTRA_OPTIONS_SLOT = 37

        // Item names
        private const val EMPTY_SLOT_NAME = ""
        private val TOGGLE_NAME_TAG_NAME = "Toggle Name Tag".mangoFmt(true)
        private val TOGGLE_ARMS_NAME = "Toggle Arms".mangoFmt(true)
        private val TOGGLE_SIZE_NAME = "Toggle Size".mangoFmt(true)
        private val TOGGLE_BASE_PLATE_NAME = "Toggle Base Plate".mangoFmt(true)
        private val EXTRA_OPTIONS_NAME = "Extra Options".mangoFmt(true)
        private const val EXTRA_OPTIONS_LORE = "COMING SOON"
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

        // Equipment slots
        inventory.setItem(HELMET_SLOT, armorStand.equipment.helmet)
        inventory.setItem(CHESTPLATE_SLOT, armorStand.equipment.chestplate)
        inventory.setItem(LEGGINGS_SLOT, armorStand.equipment.leggings)
        inventory.setItem(BOOTS_SLOT, armorStand.equipment.boots)
        inventory.setItem(MAIN_HAND_SLOT, armorStand.equipment.itemInMainHand)
        inventory.setItem(OFF_HAND_SLOT, armorStand.equipment.itemInOffHand)

        // Toggle buttons
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

        // Extra options button
        inventory.setItem(
            EXTRA_OPTIONS_SLOT,
            ItemStack.of(Material.ARMOR_STAND).name(EXTRA_OPTIONS_NAME).lore(EXTRA_OPTIONS_LORE),
        )
    }

    private fun createToggleItem(
        isActive: Boolean,
        name: String,
    ): ItemStack = ItemStack.of(if (isActive) Material.GREEN_WOOL else Material.RED_WOOL).name(name)

    private fun toggleNameTag() {
        armorStand.isCustomNameVisible = !armorStand.isCustomNameVisible
        updateToggleItem(NAME_TAG_SLOT, armorStand.isCustomNameVisible, TOGGLE_NAME_TAG_NAME)
    }

    private fun toggleArms() {
        armorStand.setArms(!armorStand.hasArms())
        updateToggleItem(ARMS_SLOT, armorStand.hasArms(), TOGGLE_ARMS_NAME)
    }

    private fun toggleSize() {
        armorStand.isSmall = !armorStand.isSmall
        updateToggleItem(SIZE_SLOT, armorStand.isSmall, TOGGLE_SIZE_NAME)
    }

    private fun toggleBasePlate() {
        armorStand.setBasePlate(!armorStand.hasBasePlate())
        updateToggleItem(BASE_PLATE_SLOT, armorStand.hasBasePlate(), TOGGLE_BASE_PLATE_NAME)
    }

    private fun updateToggleItem(
        slot: Int,
        isActive: Boolean,
        name: String,
    ) {
        inventory.setItem(slot, createToggleItem(isActive, name))
    }

    fun handleClick(slot: Int) {
        when (slot) {
            NAME_TAG_SLOT -> toggleNameTag()
            ARMS_SLOT -> toggleArms()
            SIZE_SLOT -> toggleSize()
            BASE_PLATE_SLOT -> toggleBasePlate()

            HELMET_SLOT, CHESTPLATE_SLOT, LEGGINGS_SLOT, BOOTS_SLOT, MAIN_HAND_SLOT, OFF_HAND_SLOT ->
                updateEquipment(slot)
        }
    }

    private fun updateEquipment(slot: Int) {
        val item = inventory.getItem(slot)
        val equipment = armorStand.equipment

        when (slot) {
            HELMET_SLOT -> equipment.setHelmet(item)
            CHESTPLATE_SLOT -> equipment.setChestplate(item)
            LEGGINGS_SLOT -> equipment.setLeggings(item)
            BOOTS_SLOT -> equipment.setBoots(item)
            MAIN_HAND_SLOT -> equipment.setItemInMainHand(item)
            OFF_HAND_SLOT -> equipment.setItemInOffHand(item)
        }
    }
}
