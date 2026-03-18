@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.menus

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import org.bukkit.Material
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.xodium.vanillaplus.utils.Utils.MM
import java.util.*

/** Represents an object handling mannequin equipment menu implementation within the system. */
@Suppress("UnstableApiUsage")
internal object MannequinEquipmentMenu {
    /**
     * Maps each mannequin equipment piece to its inventory slot index in the [MenuType.GENERIC_9X1] menu.
     * Slots 4, 7, and 8 are not represented here and are always filled with background panes.
     * @property index The zero-based slot index within the top inventory.
     */
    private enum class Slot(
        val index: Int,
    ) {
        HELMET(0),
        CHEST_PLATE(1),
        LEGGINGS(2),
        BOOTS(3),
        MAIN_HAND(5),
        OFF_HAND(6),
        ;

        companion object {
            /**
             * Returns the [Slot] whose [index] matches the given inventory slot number,
             * or `null` if the slot is a background pane or otherwise not an equipment slot.
             * @param index The zero-based inventory slot index to look up.
             */
            fun from(index: Int): Slot? = entries.find { it.index == index }

            /**
             * Maps a Bukkit [EquipmentSlot] to the corresponding [Slot].
             * Armor pieces route to their dedicated slot; everything else defaults to [MAIN_HAND].
             * @param equipmentSlot The equipment slot reported by the item's material.
             */
            fun from(equipmentSlot: EquipmentSlot): Slot =
                when (equipmentSlot) {
                    EquipmentSlot.HEAD -> HELMET
                    EquipmentSlot.CHEST -> CHEST_PLATE
                    EquipmentSlot.LEGS -> LEGGINGS
                    EquipmentSlot.FEET -> BOOTS
                    EquipmentSlot.OFF_HAND -> OFF_HAND
                    else -> MAIN_HAND
                }
        }
    }

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
                        setItem(Slot.MAIN_HAND.index, equipment.itemInMainHand)
                        setItem(Slot.OFF_HAND.index, equipment.itemInOffHand)
                        setItem(Slot.HELMET.index, equipment.helmet)
                        setItem(Slot.CHEST_PLATE.index, equipment.chestplate)
                        setItem(Slot.LEGGINGS.index, equipment.leggings)
                        setItem(Slot.BOOTS.index, equipment.boots)
                    }
                mannequinViews[this] = this@equipmentMenu
            }

    /**
     * Creates a single invisible black stained-glass pane used as a background filler.
     * @return A configured filler [ItemStack].
     */
    @Suppress("UnstableApiUsage")
    private fun filler(): ItemStack =
        ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).apply {
            setData(
                DataComponentTypes.TOOLTIP_DISPLAY,
                TooltipDisplay.tooltipDisplay().hideTooltip(true),
            )
        }

    /**
     * Fills all slots of this [Inventory] with invisible black stained-glass panes as a background.
     * @receiver The [Inventory] to fill.
     */
    private fun Inventory.fill() {
        for (i in 0 until size) setItem(i, filler())
    }

    /** Applies [item] to the matching [Mannequin] equipment field for the given [slot]. */
    private fun setMannequinEquipment(
        mannequin: Mannequin,
        slot: Slot,
        item: ItemStack?,
    ) {
        when (slot) {
            Slot.MAIN_HAND -> mannequin.equipment.setItemInMainHand(item)
            Slot.OFF_HAND -> mannequin.equipment.setItemInOffHand(item)
            Slot.HELMET -> mannequin.equipment.setHelmet(item)
            Slot.CHEST_PLATE -> mannequin.equipment.setChestplate(item)
            Slot.LEGGINGS -> mannequin.equipment.setLeggings(item)
            Slot.BOOTS -> mannequin.equipment.setBoots(item)
        }
    }

    /**
     * Handles the interaction with ArmorStand slots in the inventory.
     * @param event InventoryClickEvent The event triggered by the inventory click.
     */
    fun handleMannequinMenuClicking(event: InventoryClickEvent) {
        val mannequin = mannequinViews[event.view] ?: return
        val player = event.whoClicked as? Player ?: return
        val topInv = event.view.topInventory

        if (event.click.isShiftClick) {
            event.isCancelled = true
            if (event.clickedInventory == topInv) {
                val slot = Slot.from(event.slot) ?: return
                val item =
                    event.currentItem
                        ?.takeIf { !it.isEmpty && it.type != Material.BLACK_STAINED_GLASS_PANE }
                        ?: return

                if (player.inventory.addItem(item).isNotEmpty()) return

                topInv.setItem(slot.index, filler())
                setMannequinEquipment(mannequin, slot, null)
            } else {
                val item = event.currentItem?.takeIf { !it.isEmpty } ?: return
                val slot = Slot.from(item.type.equipmentSlot)
                val displaced = topInv.getItem(slot.index)

                topInv.setItem(slot.index, item)
                setMannequinEquipment(mannequin, slot, item)
                event.currentItem =
                    (if (displaced != null && displaced.type != Material.BLACK_STAINED_GLASS_PANE) displaced else null)
            }
            return
        }

        if (event.clickedInventory != topInv) return

        val slot =
            Slot.from(event.slot) ?: run {
                event.isCancelled = true
                return
            }

        setMannequinEquipment(mannequin, slot, event.cursor)
    }
}
