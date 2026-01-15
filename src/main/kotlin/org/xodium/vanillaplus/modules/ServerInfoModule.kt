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
    private val faqItems: List<ItemStack> =
        listOf(
            ItemStack.of(Material.WRITABLE_BOOK).apply {
                setData(
                    DataComponentTypes.CUSTOM_NAME,
                    MM.deserialize("<b><gradient:#FFA751:#FFE259>Rules</gradient></b>"),
                )
                setData(DataComponentTypes.LORE, ItemLore.lore(listOf(MM.deserialize("cmd: /Rules"))))
            },
            ItemStack.of(Material.PAPER).apply {
                setData(
                    DataComponentTypes.CUSTOM_NAME,
                    MM.deserialize("<b><gradient:#FFA751:#FFE259>Gamerules</gradient></b>"),
                )
                setData(
                    DataComponentTypes.LORE,
                    ItemLore.lore(
                        listOf(
                            MM.deserialize("> players_sleeping_percentage [49]"),
                            MM.deserialize(
                                "  > Requires 1 less player than half of the max players online to skip night.",
                            ),
                            MM.deserialize("> mob_griefing [false]"),
                            MM.deserialize("  > Hostile mobs cannot damage/destroy blocks."),
                        ),
                    ),
                )
            },
            ItemStack.of(Material.NAME_TAG).apply {
                setData(
                    DataComponentTypes.CUSTOM_NAME,
                    MM.deserialize("<b><gradient:#FFA751:#FFE259>Nickname</gradient></b>"),
                )
                setData(
                    DataComponentTypes.LORE,
                    ItemLore.lore(
                        listOf(
                            MM.deserialize("cmd: /nickname <name>"),
                            MM.deserialize("alias: /nick"),
                            MM.deserialize(
                                "info: Sets a custom colored nickname, use: birdflop to easily generate the code needed to execute. NOTE: don't forget to set the 'Color Format' to 'MiniMessage'. ",
                            ),
                        ),
                    ),
                )
            },
            ItemStack.of(Material.GOLDEN_HELMET).apply {
                setData(
                    DataComponentTypes.CUSTOM_NAME,
                    MM.deserialize("<b><gradient:#FFA751:#FFE259>Leaderboard</gradient></b>"),
                )
                setData(
                    DataComponentTypes.LORE,
                    ItemLore.lore(
                        listOf(
                            MM.deserialize("cmd: /leaderboard"),
                            MM.deserialize("alias: /board, /lb"),
                            MM.deserialize(
                                "info: Displays the amount of achievements completed.",
                            ),
                        ),
                    ),
                )
            },
            ItemStack.of(Material.COMPASS).apply {
                setData(
                    DataComponentTypes.CUSTOM_NAME,
                    MM.deserialize("<b><gradient:#FFA751:#FFE259>Locator</gradient></b>"),
                )
                setData(
                    DataComponentTypes.LORE,
                    ItemLore.lore(
                        listOf(
                            MM.deserialize("cmd: /locator <color|hex|reset>"),
                            MM.deserialize("alias: /lc"),
                            MM.deserialize(
                                "info: Sets the color of your locator bar icon.",
                            ),
                        ),
                    ),
                )
            },
            ItemStack.of(Material.CRIMSON_SIGN).apply {
                setData(
                    DataComponentTypes.CUSTOM_NAME,
                    MM.deserialize("<b><gradient:#FFA751:#FFE259>Sign</gradient></b>"),
                )
                setData(
                    DataComponentTypes.LORE,
                    ItemLore.lore(
                        listOf(
                            MM.deserialize("cmd: /sign <line> <text>"),
                            MM.deserialize("alias: /s"),
                            MM.deserialize(
                                "info: Sets the text of a specific line on the sign you are looking at. Supports MiniMessage formatting.",
                            ),
                        ),
                    ),
                )
            },
            ItemStack.of(Material.CHEST).apply {
                setData(
                    DataComponentTypes.CUSTOM_NAME,
                    MM.deserialize("<b><gradient:#FFA751:#FFE259>Inventory Search</gradient></b>"),
                )
                setData(
                    DataComponentTypes.LORE,
                    ItemLore.lore(
                        listOf(
                            MM.deserialize("cmd: /invsearch <material?>"),
                            MM.deserialize("alias: /search, /searchinv, /invs, /sinv"),
                            MM.deserialize("info: Searches for items in chests in a chunk."),
                        ),
                    ),
                )
            },
        )

    @Suppress("UnstableApiUsage")
    private val faqDialog: DialogLike by lazy {
        Dialog.create { builder ->
            builder
                .empty()
                .base(
                    DialogBase
                        .builder(MM.deserialize(config.serverInfoModule.faqTitle))
                        .body(faqItems.map { item -> DialogBody.item(item).build() })
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
    )
}
