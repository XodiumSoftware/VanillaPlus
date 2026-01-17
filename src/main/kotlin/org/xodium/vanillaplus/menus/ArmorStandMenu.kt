package org.xodium.vanillaplus.menus

import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.MenuType
import org.xodium.vanillaplus.utils.Utils.MM

/** Object representing the ArmorStand menu. */
internal object ArmorStandMenu {
    /**
     * Creates a menu for the given ArmorStand and Player.
     * @receiver ArmorStand The ArmorStand for which the menu is created.
     * @param player Player The player for whom the menu is created.
     * @return Inventory The created menu inventory.
     */
    @Suppress("UnstableApiUsage")
    fun ArmorStand.menu(player: Player): Inventory =
        MenuType
            .GENERIC_9X6
            .builder()
            .title(customName() ?: MM.deserialize(name))
            .build(player)
            .topInventory
            .apply {
            }
}
