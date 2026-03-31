@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.menus

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.xodium.vanillaplus.modules.RuneModule
import org.xodium.vanillaplus.pdcs.PlayerPDC.runeSlots
import org.xodium.vanillaplus.utils.Utils.MM
import java.util.*

/** Represents the rune equipment menu. */
@Suppress("UnstableApiUsage")
internal object RuneMenu {
    private val openViews: MutableSet<InventoryView> = Collections.newSetFromMap(WeakHashMap())
    private val title = MM.deserialize("<gradient:#FFA751:#FFE259><b>Rune Slots</b></gradient>")
    private val lockedSlotItems: Map<Int, ItemStack> =
        RuneModule.Config.slotLevelRequirements
            .filter { it > 0 }
            .associateWith { lockedSlotItem(it) }

    /** Returns `true` if [view] is a currently open rune menu. */
    operator fun contains(view: InventoryView): Boolean = view in openViews

    /**
     * Opens the rune equipment menu for the given player,
     * pre-populated with their currently equipped runes.
     * Slots that require a higher XP level than the player currently has are shown as locked.
     * @param player The player to open the menu for.
     */
    fun open(player: Player) {
        MenuType.HOPPER.create(player, title).also { view ->
            player.runeSlots.forEachIndexed { index, typeName ->
                val requiredLevel = RuneModule.Config.slotLevelRequirements[index]
                val item =
                    when {
                        player.level < requiredLevel -> lockedSlotItems[requiredLevel]?.clone()
                        typeName.isNotEmpty() -> RuneModule.runes.firstOrNull { it.id == typeName }?.item
                        else -> null
                    }

                item?.let { view.topInventory.setItem(index, it) }
            }
            player.openInventory(view)
            openViews.add(view)
        }
    }

    /** Returns a placeholder [ItemStack] displayed in rune slots that require [requiredLevel] to unlock. */
    private fun lockedSlotItem(requiredLevel: Int): ItemStack =
        ItemStack.of(Material.BARRIER).apply {
            setData(DataComponentTypes.ITEM_NAME, MM.deserialize("<!italic><red><b>Locked"))
            setData(
                DataComponentTypes.LORE,
                ItemLore
                    .lore()
                    .addLines(listOf(MM.deserialize("<!italic><gray>Requires xp level <white>$requiredLevel"))),
            )
            setData(DataComponentTypes.MAX_STACK_SIZE, 1)
        }
}
