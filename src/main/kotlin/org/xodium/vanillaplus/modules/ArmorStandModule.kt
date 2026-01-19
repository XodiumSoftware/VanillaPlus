@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.datacomponent.DataComponentTypes
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.*
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils.MM
import java.util.*

/** Represents a module handling armor stand mechanics within the system. */
internal object ArmorStandModule : ModuleInterface {
    private val armorStandViews = WeakHashMap<InventoryView, ArmorStand>()

    // Inventory slot constants for armorstand properties
    private const val ARMOR_STAND_NAME_TAG_ITEM_SLOT = 0
    private const val ARMOR_STAND_ARMS_ITEM_SLOT = 1
    private const val ARMOR_STAND_SMALL_ITEM_SLOT = 2
    private const val ARMOR_STAND_BASEPLATE_ITEM_SLOT = 3
    private const val ARMOR_STAND_MORE_OPTIONS_SLOT = 4

    @EventHandler
    private fun on(event: PlayerInteractAtEntityEvent) = handleArmorStandInventory(event)

    @EventHandler
    fun on(event: InventoryClickEvent) = handleArmorStandMenu(event)

    @EventHandler
    fun on(event: InventoryCloseEvent) {
        armorStandViews.remove(event.view)
    }

    @EventHandler
    fun on(event: PlayerArmorStandManipulateEvent) {
        if (event.slot != EquipmentSlot.HAND && event.slot != EquipmentSlot.OFF_HAND) return

        event.isCancelled = true

        when (event.slot) {
            EquipmentSlot.HAND -> {
                event.rightClicked.equipment.setItemInMainHand(event.playerItem)
            }

            EquipmentSlot.OFF_HAND -> {
                event.rightClicked.equipment.setItemInOffHand(event.playerItem)
            }

            else -> {}
        }

        when (event.hand) {
            EquipmentSlot.HAND -> {
                event.player.inventory.setItemInMainHand(event.armorStandItem)
            }

            EquipmentSlot.OFF_HAND -> {
                event.player.inventory.setItemInOffHand(event.armorStandItem)
            }

            else -> {}
        }
    }

    /**
     * Handles the interaction with ArmorStand slots in the inventory.
     * @param event InventoryClickEvent The event triggered by the inventory click.
     */
    private fun handleArmorStandMenu(event: InventoryClickEvent) {
        val armorStand = armorStandViews[event.view] ?: return

        event.isCancelled = true

        when (event.rawSlot) {
            // Properties Slots
            ARMOR_STAND_NAME_TAG_ITEM_SLOT -> {
                toggleArmorStandProperty(
                    armorStand,
                    event.inventory,
                    ARMOR_STAND_NAME_TAG_ITEM_SLOT,
                    { stand -> stand.isCustomNameVisible },
                    { stand, value -> stand.isCustomNameVisible = value },
                )
            }

            ARMOR_STAND_ARMS_ITEM_SLOT -> {
                toggleArmorStandProperty(
                    armorStand,
                    event.inventory,
                    ARMOR_STAND_ARMS_ITEM_SLOT,
                    { stand -> stand.hasArms() },
                    { stand, value -> stand.setArms(value) },
                )
            }

            ARMOR_STAND_SMALL_ITEM_SLOT -> {
                toggleArmorStandProperty(
                    armorStand,
                    event.inventory,
                    ARMOR_STAND_SMALL_ITEM_SLOT,
                    { stand -> stand.isSmall },
                    { stand, value -> stand.isSmall = value },
                )
            }

            ARMOR_STAND_BASEPLATE_ITEM_SLOT -> {
                toggleArmorStandProperty(
                    armorStand,
                    event.inventory,
                    ARMOR_STAND_BASEPLATE_ITEM_SLOT,
                    { stand -> stand.hasBasePlate() },
                    { stand, value -> stand.setBasePlate(value) },
                )
            }
        }
    }

    /**
     * Handles the interaction with an ArmorStand's inventory.
     * @param event EntityInteractEvent The event triggered by the interaction.
     */
    private fun handleArmorStandInventory(event: PlayerInteractAtEntityEvent) {
        val armorStand = event.rightClicked as? ArmorStand ?: return
        val player = event.player

        if (!player.isSneaking) return

        event.isCancelled = true

        val view = armorStand.menu(player)

        armorStandViews[view] = armorStand
        view.open()
    }

    /**
     * Toggles a boolean property of the [ArmorStand] and updates the corresponding inventory item.
     * @param armorStand [ArmorStand] The ArmorStand whose property is to be toggled.
     * @param inventory [Inventory] The inventory where the toggle item is located.
     * @param slot Int The slot index of the toggle item in the inventory.
     * @param getCurrentState Function to get the current state of the property.
     * @param setState Function to set the new state of the property.
     */
    @Suppress("UnstableApiUsage")
    private fun toggleArmorStandProperty(
        armorStand: ArmorStand,
        inventory: Inventory,
        slot: Int,
        getCurrentState: (ArmorStand) -> Boolean,
        setState: (ArmorStand, Boolean) -> Unit,
    ) {
        val newState = !getCurrentState(armorStand)
        val currentItem = inventory.getItem(slot) ?: return
        val displayName = currentItem.getData(DataComponentTypes.ITEM_NAME)

        setState(armorStand, newState)
        inventory.setItem(
            slot,
            ItemStack.of(if (newState) Material.GREEN_WOOL else Material.RED_WOOL).apply {
                displayName?.let { setData(DataComponentTypes.ITEM_NAME, it) }
            },
        )
    }

    /**
     * Creates a toggle item with the specified material and display name.
     * @param material [Material] The material of the item.
     * @param displayName String The display name of the item.
     * @return [ItemStack] The created toggle item.
     */
    @Suppress("UnstableApiUsage")
    private fun createToggleItem(
        material: Material,
        displayName: String,
    ): ItemStack = ItemStack.of(material).apply { setData(DataComponentTypes.ITEM_NAME, MM.deserialize(displayName)) }

    /**
     * Creates a menu for the given ArmorStand and Player.
     * @receiver ArmorStand The ArmorStand for which the menu is created.
     * @param player Player The player for whom the menu is created.
     * @return InventoryView The created menu view.
     */
    @Suppress("UnstableApiUsage")
    private fun ArmorStand.menu(player: Player): InventoryView =
        MenuType
            .HOPPER
            .builder()
            .title(customName() ?: MM.deserialize(name))
            .build(player)
            .apply {
                topInventory
                    .apply {
                        // Property Slots
                        setItem(
                            ARMOR_STAND_NAME_TAG_ITEM_SLOT,
                            createToggleItem(
                                if (isCustomNameVisible) Material.GREEN_WOOL else Material.RED_WOOL,
                                config.armorStandModule.i18n.toggleNameTagVisibility,
                            ),
                        )
                        setItem(
                            ARMOR_STAND_ARMS_ITEM_SLOT,
                            createToggleItem(
                                if (hasArms()) Material.GREEN_WOOL else Material.RED_WOOL,
                                config.armorStandModule.i18n.toggleArmsVisibility,
                            ),
                        )
                        setItem(
                            ARMOR_STAND_SMALL_ITEM_SLOT,
                            createToggleItem(
                                if (isSmall) Material.GREEN_WOOL else Material.RED_WOOL,
                                config.armorStandModule.i18n.toggleSmallArmorStand,
                            ),
                        )
                        setItem(
                            ARMOR_STAND_BASEPLATE_ITEM_SLOT,
                            createToggleItem(
                                if (hasBasePlate()) Material.GREEN_WOOL else Material.RED_WOOL,
                                config.armorStandModule.i18n.toggleBasePlateVisibility,
                            ),
                        )
                        setItem(
                            ARMOR_STAND_MORE_OPTIONS_SLOT,
                            ItemStack.of(Material.ARMOR_STAND).apply {
                                setData(
                                    DataComponentTypes.CUSTOM_NAME,
                                    MM.deserialize(config.armorStandModule.i18n.moreOptionsComingSoon),
                                )
                            },
                        )
                    }
            }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var i18n: I18n = I18n(),
    ) {
        /** Represents the internationalization settings of the module. */
        @Serializable
        data class I18n(
            var toggleNameTagVisibility: String =
                "<b><gradient:#FFA751:#FFE259>Toggle Name Tag Visibility</gradient></b>",
            var toggleArmsVisibility: String =
                "<b><gradient:#FFA751:#FFE259>Toggle Arms Visibility</gradient></b>",
            var toggleSmallArmorStand: String =
                "<b><gradient:#FFA751:#FFE259>Toggle Small ArmorStand</gradient></b>",
            var toggleBasePlateVisibility: String =
                "<b><gradient:#FFA751:#FFE259>Toggle Base Plate Visibility</gradient></b>",
            var moreOptionsComingSoon: String =
                "<b><gradient:#FFA751:#FFE259>More Options Coming Soon!</gradient></b>",
        )
    }
}
