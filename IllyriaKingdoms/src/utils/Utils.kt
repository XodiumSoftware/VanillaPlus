package org.xodium.illyriaplus.utils

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.IllyriaKingdoms
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item

/** General utilities. */
@Suppress("UnstableApiUsage")
internal object Utils {
    /** MiniMessage instance for parsing formatted strings. */
    val MM: MiniMessage = MiniMessage.miniMessage()

    /** The standardized prefix for [org.xodium.illyriaplus.IllyriaKingdoms] messages. */
    val IllyriaKingdoms.prefix: String
        get() =
            "<gradient:#FFA751:#FFE259>[</gradient><gradient:#CB2D3E:#EF473A>" +
                "${this.javaClass.simpleName}" +
                "</gradient><gradient:#FFE259:#FFA751>]</gradient>"

    /** GUI-related utilities and pre-built items for InvUI. */
    object GUI {
        /** The default GUI filler item using black stained glass panes with hidden tooltips. */
        val FILLER_ITEM: Item =
            Item
                .builder()
                .setItemProvider(
                    ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).apply {
                        setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true))
                    },
                ).build()

        /** Bound item for navigating to the previous page in the paginated GUI. */
        val PREVIOUS_PAGE_ITEM: BoundItem =
            BoundItem
                .pagedBuilder()
                .setItemProvider(
                    ItemStack.of(Material.ARROW).apply {
                        setData(DataComponentTypes.ITEM_NAME, MM.deserialize("<gray>Previous Page"))
                    },
                ).addClickHandler { _, gui, _ -> gui.page-- }
                .build()

        /** Bound item for navigating to the next page in the paginated GUI. */
        val NEXT_PAGE_ITEM: BoundItem =
            BoundItem
                .pagedBuilder()
                .setItemProvider(
                    ItemStack.of(Material.ARROW).apply {
                        setData(DataComponentTypes.ITEM_NAME, MM.deserialize("<gray>Next Page"))
                    },
                ).addClickHandler { _, gui, _ -> gui.page++ }
                .build()
    }
}
