@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus.utils

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import io.papermc.paper.registry.TypedKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.IllyriaPlus
import org.xodium.illyriaplus.pdcs.ItemPDC.selectedSpell
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item

/** General utilities. */
internal object Utils {
    /** MiniMessage instance for parsing formatted strings. */
    val MM: MiniMessage = MiniMessage.miniMessage()

    /** The standardized prefix for [IllyriaPlus] messages. */
    val IllyriaPlus.prefix: String
        get() =
            "<gradient:#FFA751:#FFE259>[</gradient><gradient:#CB2D3E:#EF473A>" +
                "${this.javaClass.simpleName}" +
                "</gradient><gradient:#FFE259:#FFA751>]</gradient>"

    /** Extension function to convert snake_case to Proper Case with spaces. */
    fun String.snakeToProperCase(): String =
        split('_').joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

    /** Extension function specifically for enchantment keys */
    fun TypedKey<Enchantment>.displayName(): Component = MM.deserialize(value().snakeToProperCase())

    /**
     * Checks if the given [item] has the specified [spell] currently selected.
     * Returns true if the item's selectedSpell matches the spell's key.
     */
    fun isSelectedSpell(
        item: ItemStack?,
        spell: Enchantment,
    ): Boolean = item?.selectedSpell == spell.key.toString()

    /** Extension function to convert CamelCase to snake_case, removing a specified suffix. */
    inline fun <reified T> Class<*>.toRegistryKeyFragment(): String =
        simpleName
            .removeSuffix(T::class.simpleName ?: "")
            .split(Regex("(?=[A-Z])"))
            .filter { it.isNotEmpty() }
            .joinToString("_") { it.lowercase() }

    /**
     * Returns the i18n string matching the current weather state of this world.
     *
     * @param thundering The string to return when it is thundering.
     * @param storm The string to return when there is a storm.
     * @param clear The string to return when the weather is clear.
     */
    fun World.weather(
        thundering: String,
        storm: String,
        clear: String,
    ): String =
        when {
            isThundering -> thundering
            hasStorm() -> storm
            else -> clear
        }

    /** GUI-related utilities and pre-built items for InvUI. */
    @Suppress("UnstableApiUsage")
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
