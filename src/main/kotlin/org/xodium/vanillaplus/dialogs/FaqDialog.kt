package org.xodium.vanillaplus.dialogs

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import io.papermc.paper.registry.data.dialog.type.DialogType
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.DialogInterface
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents an object handling faq dialog implementation within the system. */
@Suppress("UnstableApiUsage")
internal object FaqDialog : DialogInterface {
    private val faqConfig: Config
        get() =
            try {
                config.serverInfoModule.faqDialogConfig
            } catch (_: UninitializedPropertyAccessException) {
                Config()
            }

    override fun invoke(builder: DialogRegistryEntry.Builder): DialogRegistryEntry.Builder =
        builder
            .base(
                DialogBase
                    .builder(MM.deserialize(faqConfig.faqTitle))
                    .canCloseWithEscape(true)
                    .build(),
            ).type(DialogType.notice())

    /**
     * Builds a list of FAQ items from the configuration.
     * @return A list of ItemStack representing the FAQ items.
     */
    fun buildFaqItems(): List<ItemStack> =
        faqConfig.faqItems.map { entry ->
            ItemStack.of(entry.material).apply {
                if (entry.customName.isNotBlank()) {
                    setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize(entry.customName))
                }

                val loreLines = entry.lore.filter { it.isNotBlank() }

                if (loreLines.isNotEmpty()) {
                    setData(DataComponentTypes.LORE, ItemLore.lore(loreLines.map { MM.deserialize(it) }))
                }
            }
        }

    /** Represents a FAQ item in the configuration. */
    @Serializable
    data class FaqItem(
        var material: Material = Material.PAPER,
        var customName: String = "",
        var lore: List<String> = emptyList(),
    )

    @Serializable
    data class Config(
        var faqTitle: String = "<b><gradient:#CB2D3E:#EF473A>FAQ</gradient></b>",
        var faqItems: List<FaqItem> =
            listOf(
                FaqItem(
                    material = Material.WRITABLE_BOOK,
                    customName = "<b><gradient:#FFA751:#FFE259>Rules</gradient></b>",
                    lore = listOf("cmd: /Rules"),
                ),
                FaqItem(
                    material = Material.PAPER,
                    customName = "<b><gradient:#FFA751:#FFE259>Gamerules</gradient></b>",
                    lore =
                        listOf(
                            "> players_sleeping_percentage [49]",
                            "  > Requires 1 less player than half of the max players online to skip night.",
                            "> mob_griefing [false]",
                            "  > Hostile mobs cannot damage/destroy blocks.",
                        ),
                ),
                FaqItem(
                    material = Material.NAME_TAG,
                    customName = "<b><gradient:#FFA751:#FFE259>Nickname</gradient></b>",
                    lore =
                        listOf(
                            "cmd: /nickname <name>",
                            "alias: /nick",
                            "info: Sets a custom colored nickname,",
                            "  use: birdflop to easily generate the code needed to execute.",
                            "  NOTE: don't forget to set the 'Color Format' to 'MiniMessage'.",
                        ),
                ),
                FaqItem(
                    material = Material.GOLDEN_HELMET,
                    customName = "<b><gradient:#FFA751:#FFE259>Leaderboard</gradient></b>",
                    lore =
                        listOf(
                            "cmd: /leaderboard",
                            "alias: /board, /lb",
                            "info: Displays the amount of achievements completed.",
                        ),
                ),
                FaqItem(
                    material = Material.COMPASS,
                    customName = "<b><gradient:#FFA751:#FFE259>Locator</gradient></b>",
                    lore =
                        listOf(
                            "cmd: /locator <color|hex|reset>",
                            "alias: /lc",
                            "info: Sets the color of your locator bar icon.",
                        ),
                ),
                FaqItem(
                    material = Material.CRIMSON_SIGN,
                    customName = "<b><gradient:#FFA751:#FFE259>Sign</gradient></b>",
                    lore =
                        listOf(
                            "cmd: /sign <line> <text>",
                            "alias: /s",
                            "info: Sets the text of a specific line on the sign you are looking at.",
                            "  Supports MiniMessage formatting.",
                        ),
                ),
                FaqItem(
                    material = Material.CHEST,
                    customName = "<b><gradient:#FFA751:#FFE259>Inventory Search</gradient></b>",
                    lore =
                        listOf(
                            "cmd: /invsearch <material?>",
                            "alias: /search, /searchinv, /invs, /sinv",
                            "info: Searches for items in chests in a chunk.",
                        ),
                ),
            ),
    )
}
