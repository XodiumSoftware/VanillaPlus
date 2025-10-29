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
import org.bukkit.inventory.InventoryView
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

    /** Represents predefined constants for different equipment slots and provides a utility set containing all slots. */
    private object EquipmentSlot {
        const val HELMET = 13
        const val CHESTPLATE = 22
        const val LEGGINGS = 31
        const val BOOTS = 40
        const val MAIN_HAND = 21
        const val OFF_HAND = 23

        val ALL = setOf(HELMET, CHESTPLATE, LEGGINGS, BOOTS, MAIN_HAND, OFF_HAND)

        fun update(
            slot: Int,
            armorStand: ArmorStand,
            item: ItemStack?,
        ) {
            val equipment = armorStand.equipment
            when (slot) {
                HELMET -> equipment.setHelmet(item)
                CHESTPLATE -> equipment.setChestplate(item)
                LEGGINGS -> equipment.setLeggings(item)
                BOOTS -> equipment.setBoots(item)
                MAIN_HAND -> equipment.setItemInMainHand(item)
                OFF_HAND -> equipment.setItemInOffHand(item)
            }
        }
    }

    /** The `ToggleSlot` object defines constant values representing specific toggleable properties for an armour stand. */
    private object ToggleSlot {
        const val NAME_TAG = 16
        const val ARMS = 25
        const val SIZE = 34
        const val BASE_PLATE = 43
        const val EXTRA_OPTIONS = 37
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
        val inventory = armorStand.mainGui(event.player)

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
            if (event.slot in EquipmentSlot.ALL) {
                val cursorItem = event.cursor
                if (cursorItem.type != Material.AIR) {
                    if (!isValidItemForSlot(event.slot, cursorItem.type)) {
                        event.isCancelled = true
                        return
                    }
                }
                handleClick(event.slot, armorStand, inventory)
                return
            }

            event.isCancelled = true
            handleClick(event.slot, armorStand, inventory)
        } else if (event.isShiftClick) {
            event.isCancelled = true

            val item = event.currentItem ?: return
            val itemType = item.type

            getPotentialSlotsForItem(itemType)
                .find { slot -> inventory.getItem(slot)?.type?.isAir != false }
                ?.let { slot ->
                    inventory.setItem(slot, item)
                    event.currentItem = null
                    handleClick(slot, armorStand, inventory)
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
            EquipmentSlot.HELMET ->
                Tag.ITEMS_HEAD_ARMOR.isTagged(itemType) ||
                    Tag.ITEMS_SKULLS.isTagged(itemType) ||
                    itemType == Material.CARVED_PUMPKIN

            EquipmentSlot.CHESTPLATE -> Tag.ITEMS_CHEST_ARMOR.isTagged(itemType) || itemType == Material.ELYTRA
            EquipmentSlot.LEGGINGS -> Tag.ITEMS_LEG_ARMOR.isTagged(itemType)
            EquipmentSlot.BOOTS -> Tag.ITEMS_FOOT_ARMOR.isTagged(itemType)
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
                itemType == Material.CARVED_PUMPKIN -> listOf(EquipmentSlot.HELMET)

            Tag.ITEMS_CHEST_ARMOR.isTagged(itemType) || itemType == Material.ELYTRA -> listOf(EquipmentSlot.CHESTPLATE)
            Tag.ITEMS_LEG_ARMOR.isTagged(itemType) -> listOf(EquipmentSlot.LEGGINGS)
            Tag.ITEMS_FOOT_ARMOR.isTagged(itemType) -> listOf(EquipmentSlot.BOOTS)
            else -> listOf(EquipmentSlot.MAIN_HAND, EquipmentSlot.OFF_HAND)
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

    /**
     * Updates a toggle item in the inventory.
     * @param slot The slot of the item to update.
     * @param isActive The new state of the toggle.
     * @param name The name of the toggle item.
     * @param inventory The inventory to update.
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
     * @param armorStand The armour stand that was clicked.
     * @param inventory The inventory that was clicked within.
     */
    private fun handleClick(
        slot: Int,
        armorStand: ArmorStand,
        inventory: Inventory,
    ) {
        instance.server.scheduler.runTask(
            instance,
            Runnable {
                when (slot) {
                    ToggleSlot.NAME_TAG -> armorStand.toggleNameTag(inventory)
                    ToggleSlot.ARMS -> armorStand.toggleArms(inventory)
                    ToggleSlot.SIZE -> armorStand.toggleSize(inventory)
                    ToggleSlot.BASE_PLATE -> armorStand.toggleBasePlate(inventory)

                    in EquipmentSlot.ALL -> EquipmentSlot.update(slot, armorStand, inventory.getItem(slot))
                }
            },
        )
    }

    /** Toggles the visibility of the armour stand's name tag. */
    private fun ArmorStand.toggleNameTag(inventory: Inventory) {
        isCustomNameVisible = !isCustomNameVisible
        updateToggleItem(ToggleSlot.NAME_TAG, isCustomNameVisible, config.i18n.toggleNameTag, inventory)
    }

    /** Toggles whether the armour stand has arms. */
    private fun ArmorStand.toggleArms(inventory: Inventory) {
        setArms(!hasArms())
        updateToggleItem(ToggleSlot.ARMS, hasArms(), config.i18n.toggleArms, inventory)
    }

    /** Toggles the size of the armour stand. */
    private fun ArmorStand.toggleSize(inventory: Inventory) {
        isSmall = !isSmall
        updateToggleItem(ToggleSlot.SIZE, isSmall, config.i18n.toggleSize, inventory)
    }

    /** Toggles the visibility of the armour stand's baseplate. */
    private fun ArmorStand.toggleBasePlate(inventory: Inventory) {
        setBasePlate(!hasBasePlate())
        updateToggleItem(ToggleSlot.BASE_PLATE, hasBasePlate(), config.i18n.toggleBasePlate, inventory)
    }

    /**
     * Opens a custom inventory view for the specified player, based on the properties of the armour stand.
     * @param player The player for whom the inventory view is being created.
     * @return The created inventory view associated with the player.
     */
    @Suppress("UnstableApiUsage")
    private fun ArmorStand.view(player: Player): InventoryView =
        MenuType.GENERIC_9X6
            .builder()
            .title(customName() ?: name.mm())
            .checkReachable(true)
            .location(location)
            .build(player)

    /**
     * Creates and populates the main customization GUI for an armour stand for the specified player.
     * @param player The player for whom the GUI is being created.
     * @return The populated inventory instance representing the GUI.
     */
    private fun ArmorStand.mainGui(player: Player): Inventory =
        view(player).topInventory.apply {
            fill(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).name(config.i18n.emptySlotName))

            // Equipment slots
            setItem(EquipmentSlot.HELMET, equipment.helmet)
            setItem(EquipmentSlot.CHESTPLATE, equipment.chestplate)
            setItem(EquipmentSlot.LEGGINGS, equipment.leggings)
            setItem(EquipmentSlot.BOOTS, equipment.boots)
            setItem(EquipmentSlot.MAIN_HAND, equipment.itemInMainHand)
            setItem(EquipmentSlot.OFF_HAND, equipment.itemInOffHand)

            // Toggle buttons
            setItem(
                ToggleSlot.NAME_TAG,
                createToggleItem(isCustomNameVisible, config.i18n.toggleNameTag),
            )
            setItem(
                ToggleSlot.ARMS,
                createToggleItem(hasArms(), config.i18n.toggleArms),
            )
            setItem(
                ToggleSlot.SIZE,
                createToggleItem(isSmall, config.i18n.toggleSize),
            )
            setItem(
                ToggleSlot.BASE_PLATE,
                createToggleItem(hasBasePlate(), config.i18n.toggleBasePlate),
            )

            // Extra options button
            setItem(
                ToggleSlot.EXTRA_OPTIONS,
                ItemStack
                    .of(Material.ARMOR_STAND)
                    .name(config.i18n.extraOptionsName)
                    .lore(config.i18n.extraOptionsLore),
            )
        }

    /**
     * Creates and populates the pose customization GUI for an armour stand for the specified player.
     * @param player The player for whom the GUI is being created.
     * @return The populated inventory instance representing the pose customization GUI.
     */
    private fun ArmorStand.poseGui(player: Player): Inventory =
        view(player).topInventory.apply {
            fill(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).name(config.i18n.emptySlotName))
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
