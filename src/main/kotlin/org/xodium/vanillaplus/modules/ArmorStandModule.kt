@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.math.Rotations
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
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import java.util.*

/** Represents a module handling armour stand mechanics within the system. */
internal class ArmorStandModule : ModuleInterface<ArmorStandModule.Config> {
    override val config: Config = Config()

    private val armorStandGuis = WeakHashMap<Inventory, ArmorStand>()

    // TODO: move to pdc.
    private val armorStandBodyPart = WeakHashMap<Inventory, BodyPart>()

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

    /** The `ToggleSlot` object defines constant values representing specific toggleable properties for an [ArmorStand]. */
    private object ToggleSlot {
        const val NAME_TAG = 16
        const val ARMS = 25
        const val SIZE = 34
        const val BASE_PLATE = 43
        const val BODY_PART = 38
    }

    /** Represents predefined rotation slot values for an [ArmorStand]. */
    private object RotationSlot {
        const val X_AXIS = 10
        const val Y_AXIS = 19
        const val Z_AXIS = 28
        const val RESET = 37

        val AXIS = setOf(X_AXIS, Y_AXIS, Z_AXIS)
    }

    /**
     * Represents the body parts of an ArmorStand for customization and interaction purposes.
     * @property displayName The display name of the body part.
     */
    private enum class BodyPart(
        val displayName: String,
    ) {
        HEAD("Head"),
        BODY("Body"),
        RIGHT_ARM("Right Arm"),
        LEFT_ARM("Left Arm"),
        RIGHT_LEG("Right Leg"),
        LEFT_LEG("Left Leg"),
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
            handleClick(event.slot, armorStand, inventory, event)
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
    private fun toggleItem(
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
        inventory.setItem(slot, toggleItem(isActive, name))
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
        event: InventoryClickEvent? = null,
    ) {
        instance.server.scheduler.runTask(
            instance,
            Runnable {
                when (slot) {
                    ToggleSlot.NAME_TAG -> armorStand.toggleNameTag(inventory)
                    ToggleSlot.ARMS -> armorStand.toggleArms(inventory)
                    ToggleSlot.SIZE -> armorStand.toggleSize(inventory)
                    ToggleSlot.BASE_PLATE -> armorStand.toggleBasePlate(inventory)
                    ToggleSlot.BODY_PART -> toggleBodyPart(inventory)

                    in RotationSlot.AXIS -> {
                        if (event == null) return@Runnable

                        val bodyPart = armorStandBodyPart.getOrDefault(inventory, BodyPart.HEAD)
                        val currentRotation = armorStand.getRotation(bodyPart)

                        var amount = Math.toRadians(15.0)

                        if (event.isShiftClick) amount = Math.toRadians(45.0)
                        if (event.isRightClick) amount *= -1

                        val x = currentRotation.x()
                        val y = currentRotation.y()
                        val z = currentRotation.z()
                        val newRotation =
                            when (slot) {
                                RotationSlot.X_AXIS -> Rotations.ofDegrees((x + amount), y, z)
                                RotationSlot.Y_AXIS -> Rotations.ofDegrees(x, (y + amount), z)
                                RotationSlot.Z_AXIS -> Rotations.ofDegrees(x, y, (z + amount))
                                else -> currentRotation
                            }
                        armorStand.changeRotation(bodyPart, newRotation)
                    }

                    RotationSlot.RESET -> {
                        // TODO: make it double click.
                        val bodyPart = armorStandBodyPart.getOrDefault(inventory, BodyPart.HEAD)
                        armorStand.changeRotation(bodyPart, Rotations.ZERO)
                    }

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
     * Toggles the current selected body part of the [ArmorStand] for the provided inventory.
     * @param inventory The inventory associated with the [ArmorStand]. Used to determine and update the selected body part.
     */
    private fun toggleBodyPart(inventory: Inventory) {
        val currentPart = armorStandBodyPart.getOrDefault(inventory, BodyPart.HEAD)
        val nextPart = BodyPart.entries[(currentPart.ordinal + 1) % BodyPart.entries.size]
        armorStandBodyPart[inventory] = nextPart
        inventory.setItem(
            ToggleSlot.BODY_PART,
            ItemStack.of(Material.BONE).name(config.i18n.bodyPart.format(nextPart.displayName)),
        )
    }

    /**
     * Changes the rotation of a specific body part of the ArmorStand.
     * @param bodyPart The body part of the ArmorStand to update the rotation for.
     * @param rotations The new rotation values to apply to the specified body part.
     */
    private fun ArmorStand.changeRotation(
        bodyPart: BodyPart,
        rotations: Rotations,
    ) {
        when (bodyPart) {
            BodyPart.HEAD -> headRotations = rotations
            BodyPart.BODY -> bodyRotations = rotations
            BodyPart.RIGHT_ARM -> rightArmRotations = rotations
            BodyPart.LEFT_ARM -> leftArmRotations = rotations
            BodyPart.RIGHT_LEG -> rightLegRotations = rotations
            BodyPart.LEFT_LEG -> leftLegRotations = rotations
        }
    }

    /**
     * Retrieves the current rotation of the specified body part of an [ArmorStand].
     * @param bodyPart The body part of the [ArmorStand] for which the rotation is being retrieved.
     * @return The current rotation values of the specified body part.
     */
    private fun ArmorStand.getRotation(bodyPart: BodyPart): Rotations =
        when (bodyPart) {
            BodyPart.HEAD -> headRotations
            BodyPart.BODY -> bodyRotations
            BodyPart.RIGHT_ARM -> rightArmRotations
            BodyPart.LEFT_ARM -> leftArmRotations
            BodyPart.RIGHT_LEG -> rightLegRotations
            BodyPart.LEFT_LEG -> leftLegRotations
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
     * Generates and returns a custom GUI inventory for a player based on the state and properties of the [ArmorStand].
     * @param player The player for whom the inventory GUI is being created.
     * @return The created and populated custom inventory.
     */
    private fun ArmorStand.gui(player: Player): Inventory =
        view(player).topInventory.apply {
            fill(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).name(config.i18n.filler))

            // Equipment slots
            setItem(EquipmentSlot.HELMET, equipment.helmet)
            setItem(EquipmentSlot.CHESTPLATE, equipment.chestplate)
            setItem(EquipmentSlot.LEGGINGS, equipment.leggings)
            setItem(EquipmentSlot.BOOTS, equipment.boots)
            setItem(EquipmentSlot.MAIN_HAND, equipment.itemInMainHand)
            setItem(EquipmentSlot.OFF_HAND, equipment.itemInOffHand)

            // Toggle buttons
            setItem(ToggleSlot.NAME_TAG, toggleItem(isCustomNameVisible, config.i18n.toggleNameTag))
            setItem(ToggleSlot.ARMS, toggleItem(hasArms(), config.i18n.toggleArms))
            setItem(ToggleSlot.SIZE, toggleItem(isSmall, config.i18n.toggleSize))
            setItem(ToggleSlot.BASE_PLATE, toggleItem(hasBasePlate(), config.i18n.toggleBasePlate))
            setItem(
                ToggleSlot.BODY_PART,
                ItemStack
                    .of(Material.BONE)
                    .name(config.i18n.bodyPart.format(armorStandBodyPart.getOrPut(this) { BodyPart.HEAD }.displayName)),
            )

            // Rotation buttons
            setItem(RotationSlot.X_AXIS, ItemStack.of(Material.RED_WOOL).name(config.i18n.rotateX))
            setItem(RotationSlot.Y_AXIS, ItemStack.of(Material.RED_WOOL).name(config.i18n.rotateY))
            setItem(RotationSlot.Z_AXIS, ItemStack.of(Material.RED_WOOL).name(config.i18n.rotateZ))
            setItem(
                RotationSlot.RESET,
                ItemStack.of(Material.RED_WOOL).name(config.i18n.reset).lore(config.i18n.resetLore),
            )
        }

    data class Config(
        override var enabled: Boolean = true,
        var i18n: I18n = I18n(),
    ) : ModuleInterface.Config {
        data class I18n(
            var filler: String = "",
            var toggleNameTag: String = "Toggle Name Tag".mangoFmt(true),
            var toggleArms: String = "Toggle Arms".mangoFmt(true),
            var toggleSize: String = "Toggle Size".mangoFmt(true),
            var toggleBasePlate: String = "Toggle Base Plate".mangoFmt(true),
            var bodyPart: String = "Body Part: %s".mangoFmt(true),
            var rotateX: String = "Rotate X".mangoFmt(true),
            var rotateY: String = "Rotate Y".mangoFmt(true),
            var rotateZ: String = "Rotate Z".mangoFmt(true),
            var reset: String = "Reset".fireFmt(),
            var resetLore: String = "Click twice to Reset",
        )
    }
}
