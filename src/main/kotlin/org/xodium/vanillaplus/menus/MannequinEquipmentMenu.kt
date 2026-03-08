package org.xodium.vanillaplus.menus

import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.MenuType
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents an object handling mannequin equipment menu implementation within the system. */
internal object MannequinEquipmentMenu {
    /**
     * Creates and returns a configured equipment menu for editing the gear of this [Mannequin].
     * @receiver The [Mannequin] instance for which the menu is created.
     * @param player The [Player] for whom the inventory view is built.
     * @return A fully configured [InventoryView] bound to this mannequin.
     */
    @Suppress("UnstableApiUsage")
    fun Mannequin.equipmentMenu(player: Player): InventoryView =
        MenuType.HOPPER
            .builder()
            .title(MM.deserialize("<b><gradient:#CB2D3E:#EF473A>Mannequin Equipment Editor</gradient></b>"))
            .build(player)
}
