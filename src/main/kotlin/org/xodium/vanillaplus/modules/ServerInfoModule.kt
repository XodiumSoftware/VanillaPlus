package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import kotlinx.serialization.Serializable
import net.kyori.adventure.dialog.DialogLike
import org.bukkit.Material
import org.bukkit.ServerLinks
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import org.xodium.vanillaplus.utils.Utils.MM
import java.net.URI

/** Represents a module handling server info mechanics within the system. */
internal object ServerInfoModule : ModuleInterface {
    @Suppress("UnstableApiUsage")
    private val faqDialog: DialogLike by lazy {
        Dialog.create { builder ->
            builder
                .empty()
                .base(
                    DialogBase
                        .builder(MM.deserialize(config.serverInfoModule.faqTitle))
                        .body(buildFaqItems().map { item -> DialogBody.item(item).build() })
                        .canCloseWithEscape(true)
                        .build(),
                ).type(DialogType.notice())
        }
    }

    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("faq")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .playerExecuted { player, _ -> player.showDialog(faqDialog) },
                "Opens the FAQ interface",
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.faq".lowercase(),
                "Allows use of the faq command",
                PermissionDefault.TRUE,
            ),
        )

    init {
        serverLinks()
    }

    /** Configures server links based on the module's configuration. */
    @Suppress("UnstableApiUsage")
    private fun serverLinks() =
        config.serverInfoModule.serverLinks.forEach { (type, url) ->
            runCatching { URI.create(url) }.getOrNull()?.let { instance.server.serverLinks.setLink(type, it) }
        }

    @Suppress("UnstableApiUsage")
    private fun buildFaqItems(): List<ItemStack> =
        config.serverInfoModule.faqItems.map { entry ->
            ItemStack.of(entry.material).apply {
                if (entry.customName.isNotBlank()) {
                    setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize(entry.customName))
                }

                val loreLines = entry.lore.filter { it.isNotBlank() }

                if (loreLines.isNotEmpty()) {
                    setData(DataComponentTypes.LORE, ItemLore.lore(loreLines.map(MM::deserialize)))
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

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        @Suppress("UnstableApiUsage") var serverLinks: Map<ServerLinks.Type, String> =
            mapOf(
                ServerLinks.Type.WEBSITE to "https://xodium.org/",
                ServerLinks.Type.REPORT_BUG to "https://github.com/XodiumSoftware/VanillaPlus/issues",
                ServerLinks.Type.STATUS to "https://mcsrvstat.us/server/illyria.xodium.org",
                ServerLinks.Type.COMMUNITY to "https://discord.gg/jusYH9aYUh",
            ),
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
