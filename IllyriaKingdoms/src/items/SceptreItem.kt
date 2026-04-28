package org.xodium.illyriaplus.items

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.guis.MainGui
import org.xodium.illyriaplus.interfaces.ItemInterface
import org.xodium.illyriaplus.pdcs.ItemPDC.isSceptre

/** Kingdom Tool item (Sceptre) used for accessing kingdom management GUI. */
internal object SceptreItem : ItemInterface {
    /** The kingdom tool item stack with custom name, lore, and PDC flag. */
    @Suppress("UnstableApiUsage")
    override val item =
        ItemStack.of(Material.STICK).apply {
            setData(
                DataComponentTypes.ITEM_NAME,
                mm.deserialize("<gradient:#FFA751:#FFE259>Kingdom Tool</gradient>"),
            )
            setData(
                DataComponentTypes.LORE,
                ItemLore.lore(
                    listOf(
                        mm.deserialize("<gray>Right-click to open your kingdom menu.</gray>"),
                        mm.deserialize("<yellow>Manage your territory and claims!</yellow>"),
                    ),
                ),
            )
            isSceptre = true
        }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return

        val item = event.item ?: return

        if (!item.isSceptre) return

        event.isCancelled = true

        MainGui.window.open(event.player)
    }
}
