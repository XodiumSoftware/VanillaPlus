@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.menus

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import org.bukkit.Material
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.xodium.vanillaplus.utils.Utils.MM
import java.util.*

/** Represents an object handling mannequin equipment menu implementation within the system. */
internal object MannequinEquipmentMenu {
    private const val MANNEQUIN_MAIN_HAND_SLOT = 5
    private const val MANNEQUIN_OFF_HAND_SLOT = 6
    private const val MANNEQUIN_HELMET_SLOT = 0
    private const val MANNEQUIN_CHEST_PLATE_SLOT = 1
    private const val MANNEQUIN_LEGGINGS_SLOT = 2
    private const val MANNEQUIN_BOOTS_SLOT = 3

    private val mannequinViews = WeakHashMap<InventoryView, Mannequin>()

    /**
     * Creates and returns a configured equipment menu for editing the gear of this [Mannequin].
     * @receiver The [Mannequin] instance for which the menu is created.
     * @param player The [Player] for whom the inventory view is built.
     * @return A fully configured [InventoryView] bound to this mannequin.
     */
    @Suppress("UnstableApiUsage")
    fun Mannequin.equipmentMenu(player: Player): InventoryView =
        MenuType.GENERIC_9X1
            .builder()
            .title(MM.deserialize("<b><gradient:#CB2D3E:#EF473A>Mannequin Equipment Editor</gradient></b>"))
            .build(player)
            .apply {
                topInventory
                    .apply {
                        fill()
                        setItem(MANNEQUIN_MAIN_HAND_SLOT, equipment.itemInMainHand)
                        setItem(MANNEQUIN_OFF_HAND_SLOT, equipment.itemInOffHand)
                        setItem(MANNEQUIN_HELMET_SLOT, equipment.helmet)
                        setItem(MANNEQUIN_CHEST_PLATE_SLOT, equipment.chestplate)
                        setItem(MANNEQUIN_LEGGINGS_SLOT, equipment.leggings)
                        setItem(MANNEQUIN_BOOTS_SLOT, equipment.boots)
                    }
                mannequinViews[this] = this@equipmentMenu
            }

    /**
     * Fills all slots of this [Inventory] with invisible black stained-glass panes as a background.
     * @receiver The [Inventory] to fill.
     */
    private fun Inventory.fill() {
        for (i in 0 until size) {
            setItem(
                i,
                @Suppress("UnstableApiUsage")
                ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).apply {
                    setData(
                        DataComponentTypes.TOOLTIP_DISPLAY,
                        TooltipDisplay.tooltipDisplay().hideTooltip(true),
                    )
                },
            )
        }
    }

    /**
     * Handles the interaction with ArmorStand slots in the inventory.
     * @param event InventoryClickEvent The event triggered by the inventory click.
     */
    fun handleMannequinMenuClicking(event: InventoryClickEvent) {
        val mannequin = mannequinViews[event.view] ?: return

        if (event.click.isShiftClick) event.isCancelled = true // TODO: Handle shift-clicking better in v2.
        if (event.clickedInventory != event.view.topInventory) return

        when (event.slot) {
            MANNEQUIN_MAIN_HAND_SLOT -> mannequin.equipment.setItemInMainHand(event.cursor)
            MANNEQUIN_OFF_HAND_SLOT -> mannequin.equipment.setItemInOffHand(event.cursor)
            MANNEQUIN_HELMET_SLOT -> mannequin.equipment.setHelmet(event.cursor)
            MANNEQUIN_CHEST_PLATE_SLOT -> mannequin.equipment.setChestplate(event.cursor)
            MANNEQUIN_LEGGINGS_SLOT -> mannequin.equipment.setLeggings(event.cursor)
            MANNEQUIN_BOOTS_SLOT -> mannequin.equipment.setBoots(event.cursor)
            else -> event.isCancelled = true
        }
    }
}
