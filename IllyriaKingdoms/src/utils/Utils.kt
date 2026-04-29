package org.xodium.illyriaplus.utils

import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.IllyriaKingdoms
import xyz.xenondevs.invui.item.Item

/** General utilities. */
internal object Utils {
    /** MiniMessage instance for parsing formatted strings. */
    val MM: MiniMessage = MiniMessage.miniMessage()

    /** The standardized prefix for [org.xodium.illyriaplus.IllyriaKingdoms] messages. */
    val IllyriaKingdoms.prefix: String
        get() =
            "<gradient:#FFA751:#FFE259>[</gradient><gradient:#CB2D3E:#EF473A>" +
                "${this.javaClass.simpleName}" +
                "</gradient><gradient:#FFE259:#FFA751>]</gradient>"

    /** The default GUI filler item. */
    val GUI_FILLER_ITEM: Item =
        Item
            .builder()
            .setItemProvider(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE))
            .build()

    /**
     * Creates a name tag item stack for GUI rename functionality.
     *
     * @param title The title to display on the name tag.
     * @return An [ItemStack] configured as a name tag with the given title.
     */
    @Suppress("UnstableApiUsage")
    fun guiRenameItemStack(title: String): ItemStack =
        ItemStack.of(Material.NAME_TAG).apply { setData(DataComponentTypes.ITEM_NAME, MM.deserialize(title)) }
}
