@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.menus

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.MenuType
import org.xodium.vanillaplus.modules.RuneModule
import org.xodium.vanillaplus.pdcs.PlayerPDC.runeSlots
import org.xodium.vanillaplus.utils.Utils.MM
import java.util.*

/** Represents the rune equipment menu. */
@Suppress("UnstableApiUsage")
internal object RuneMenu {
    val openViews: WeakHashMap<InventoryView, Unit> = WeakHashMap()

    private val TITLE: Component = MM.deserialize("<!italic><gold>Rune Slots</gold>")

    /**
     * Opens the rune equipment menu for the given player,
     * pre-populated with their currently equipped runes.
     * @param player The player to open the menu for.
     */
    fun open(player: Player) {
        val view = MenuType.HOPPER.create(player, TITLE)

        player.runeSlots.forEachIndexed { index, typeName ->
            if (typeName.isNotEmpty()) {
                RuneModule.RuneType.entries.firstOrNull { it.name == typeName }?.let {
                    view.topInventory.setItem(index, RuneModule.createGemForType(it))
                }
            }
        }
        player.openInventory(view)
        openViews[view] = Unit
    }
}
