@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.fill
import org.xodium.vanillaplus.utils.ExtUtils.lore
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.name
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import java.util.*

/** Represents a module handling armour stand mechanics within the system. */
internal class ArmorStandModule : ModuleInterface<ArmorStandModule.Config> {
    override val config: Config = Config()

    private val armorStandGuis = WeakHashMap<Inventory, ArmorStand>()

    companion object {
        // Slot positions for equipment
        const val HELMET_SLOT = 13
        const val CHESTPLATE_SLOT = 22
        const val LEGGINGS_SLOT = 31
        const val BOOTS_SLOT = 40
        const val MAIN_HAND_SLOT = 21
        const val OFF_HAND_SLOT = 23

        val EQUIPMENT_SLOTS =
            setOf(
                HELMET_SLOT,
                CHESTPLATE_SLOT,
                LEGGINGS_SLOT,
                BOOTS_SLOT,
                MAIN_HAND_SLOT,
                OFF_HAND_SLOT,
            )

        // Slot positions for toggle buttons
        const val NAME_TAG_SLOT = 16
        const val ARMS_SLOT = 25
        const val SIZE_SLOT = 34
        const val BASE_PLATE_SLOT = 43

        // Slot positions for the extra options button
        const val EXTRA_OPTIONS_SLOT = 37
    }

    @EventHandler
    fun on(event: PlayerInteractAtEntityEvent) {
        if (!enabled() ||
            event.rightClicked !is ArmorStand ||
            event.player.isSneaking ||
            event.player.inventory.itemInMainHand.type == Material.NAME_TAG
        ) {
            return
        }

        val armorStand = event.rightClicked as ArmorStand
        val inventory = armorStand.gui(event.player)

        armorStandGuis[inventory] = armorStand

        event.player.openInventory(inventory)
        event.isCancelled = true
    }

    @EventHandler
    fun on(event: InventoryClickEvent) {
        if (!enabled()) return

        val inventory = event.inventory
        val armorStand = armorStandGuis[inventory] ?: return
        val clickedInventory = event.clickedInventory

        if (clickedInventory === inventory) {
            if (event.slot in EQUIPMENT_SLOTS) {
                val cursorItem = event.cursor
                if (cursorItem.type != Material.AIR) {
                    if (!isValidItemForSlot(event.slot, cursorItem.type)) {
                        event.isCancelled = true
                        return
                    }
                }
                instance.server.scheduler.runTask(instance, Runnable { handleClick(event.slot, armorStand, inventory) })
                return
            }

            event.isCancelled = true
            handleClick(event.slot, armorStand, inventory)
        } else if (event.isShiftClick) {
            event.isCancelled = true

            val item = event.currentItem ?: return
            val itemType = item.type

            for (slot in getPotentialSlotsForItem(itemType)) {
                if (inventory.getItem(slot).let { it == null || it.type == Material.AIR }) {
                    inventory.setItem(slot, item.clone())
                    event.currentItem = null
                    instance.server.scheduler.runTask(instance, Runnable { handleClick(slot, armorStand, inventory) })
                    break
                }
            }
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

    /**
     * Gets the potential equipment slots for a given item type.
     * @param itemType The material of the item to check.
     * @return A list of potential equipment slots for the item.
     */
    private fun getPotentialSlotsForItem(itemType: Material): List<Int> =
        when {
            Tag.ITEMS_HEAD_ARMOR.isTagged(itemType) ||
                Tag.ITEMS_SKULLS.isTagged(itemType) ||
                itemType == Material.CARVED_PUMPKIN -> listOf(HELMET_SLOT)

            Tag.ITEMS_CHEST_ARMOR.isTagged(itemType) || itemType == Material.ELYTRA -> listOf(CHESTPLATE_SLOT)
            Tag.ITEMS_LEG_ARMOR.isTagged(itemType) -> listOf(LEGGINGS_SLOT)
            Tag.ITEMS_FOOT_ARMOR.isTagged(itemType) -> listOf(BOOTS_SLOT)
            else -> listOf(MAIN_HAND_SLOT, OFF_HAND_SLOT)
        }

    @Suppress("UnstableApiUsage")
    fun ArmorStand.gui(player: Player): Inventory {
        val view =
            MenuType.GENERIC_9X6
                .builder()
                .title(customName() ?: name.mm())
                .checkReachable(true)
                .location(location)
                .build(player)
        val inventory = view.topInventory

        inventory.fill(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).name(config.i18n.emptySlotName))

        // Equipment slots
        inventory.setItem(HELMET_SLOT, equipment.helmet)
        inventory.setItem(CHESTPLATE_SLOT, equipment.chestplate)
        inventory.setItem(LEGGINGS_SLOT, equipment.leggings)
        inventory.setItem(BOOTS_SLOT, equipment.boots)
        inventory.setItem(MAIN_HAND_SLOT, equipment.itemInMainHand)
        inventory.setItem(OFF_HAND_SLOT, equipment.itemInOffHand)

        // Toggle buttons
        inventory.setItem(
            NAME_TAG_SLOT,
            createToggleItem(isCustomNameVisible, config.i18n.toggleNameTag),
        )
        inventory.setItem(
            ARMS_SLOT,
            createToggleItem(hasArms(), config.i18n.toggleArms),
        )
        inventory.setItem(
            SIZE_SLOT,
            createToggleItem(isSmall, config.i18n.toggleSize),
        )
        inventory.setItem(
            BASE_PLATE_SLOT,
            createToggleItem(hasBasePlate(), config.i18n.toggleBasePlate),
        )

        // Extra options button
        inventory.setItem(
            EXTRA_OPTIONS_SLOT,
            ItemStack.of(Material.ARMOR_STAND).name(config.i18n.extraOptionsName).lore(config.i18n.extraOptionsLore),
        )

        return inventory
    }

    /**
     * Creates an [ItemStack] for a toggle button.
     * @param isActive Whether the toggle is currently active.
     * @param name The name of the toggle item.
     * @return The created [ItemStack].
     */
    private fun createToggleItem(
        isActive: Boolean,
        name: String,
    ): ItemStack = ItemStack.of(if (isActive) Material.GREEN_WOOL else Material.RED_WOOL).name(name)

    /** Toggles the visibility of the armour stand's name tag. */
    private fun ArmorStand.toggleNameTag(inventory: Inventory) {
        isCustomNameVisible = !isCustomNameVisible
        updateToggleItem(NAME_TAG_SLOT, isCustomNameVisible, config.i18n.toggleNameTag, inventory)
    }

    /** Toggles whether the armour stand has arms. */
    private fun ArmorStand.toggleArms(inventory: Inventory) {
        setArms(!hasArms())
        updateToggleItem(ARMS_SLOT, hasArms(), config.i18n.toggleArms, inventory)
    }

    /** Toggles the size of the armour stand. */
    private fun ArmorStand.toggleSize(inventory: Inventory) {
        isSmall = !isSmall
        updateToggleItem(SIZE_SLOT, isSmall, config.i18n.toggleSize, inventory)
    }

    /** Toggles the visibility of the armour stand's baseplate. */
    private fun ArmorStand.toggleBasePlate(inventory: Inventory) {
        setBasePlate(!hasBasePlate())
        updateToggleItem(BASE_PLATE_SLOT, hasBasePlate(), config.i18n.toggleBasePlate, inventory)
    }

    /**
     * Updates a toggle item in the inventory.
     * @param slot The slot of the item to update.
     * @param isActive The new state of the toggle.
     * @param name The name of the toggle item.
     */
    private fun updateToggleItem(
        slot: Int,
        isActive: Boolean,
        name: String,
        inventory: Inventory,
    ) {
        inventory.setItem(slot, createToggleItem(isActive, name))
    }

    /**
     * Handles a click event within the inventory.
     * @param slot The slot that was clicked.
     */
    fun handleClick(
        slot: Int,
        armorStand: ArmorStand,
        inventory: Inventory,
    ) {
        when (slot) {
            NAME_TAG_SLOT -> armorStand.toggleNameTag(inventory)
            ARMS_SLOT -> armorStand.toggleArms(inventory)
            SIZE_SLOT -> armorStand.toggleSize(inventory)
            BASE_PLATE_SLOT -> armorStand.toggleBasePlate(inventory)

            HELMET_SLOT, CHESTPLATE_SLOT, LEGGINGS_SLOT, BOOTS_SLOT, MAIN_HAND_SLOT, OFF_HAND_SLOT ->
                updateEquipment(slot, armorStand, inventory)
        }
    }

    /**
     * Updates the armour stand's equipment from the inventory.
     * @param slot The slot corresponding to the equipment piece that was changed.
     */
    private fun updateEquipment(
        slot: Int,
        armorStand: ArmorStand,
        inventory: Inventory,
    ) {
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

    data class Config(
        override var enabled: Boolean = true,
        var i18n: I18n = I18n(),
    ) : ModuleInterface.Config {
        data class I18n(
            var emptySlotName: String = "",
            var toggleNameTag: String = "Toggle Name Tag".mangoFmt(true),
            var toggleArms: String = "Toggle Arms".mangoFmt(true),
            var toggleSize: String = "Toggle Size".mangoFmt(true),
            var toggleBasePlate: String = "Toggle Base Plate".mangoFmt(true),
            var extraOptionsName: String = "Extra Options".mangoFmt(true),
            var extraOptionsLore: String = "COMING SOON",
        )
    }
}
