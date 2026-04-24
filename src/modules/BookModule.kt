package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.permissions.Permission
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.BookData
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted

/** Represents a module handling book mechanics within the system. */
internal object BookModule : ModuleInterface {
    override val cmds
        get() =
            Config.BOOKS.map { book ->
                CommandData(
                    Commands
                        .literal(book.cmd.lowercase())
                        .requires { it.sender.hasPermission("${Config.PERM_PREFIX}.${book.cmd.lowercase()}") }
                        .playerExecuted { player, _ -> player.openBook(book.toBook()) },
                    "Opens the predefined book '${book.cmd.lowercase()}'",
                )
            }

    override val perms
        get() =
            Config.BOOKS.map {
                Permission(
                    "${Config.PERM_PREFIX}.${it.cmd.lowercase()}",
                    "Allows use of the book command: ${it.cmd}",
                    it.permission,
                )
            }

    /** Represents the config of the module. */
    object Config {
        /** The permission prefix for book commands. */
        val PERM_PREFIX: String = "${instance.javaClass.simpleName}.book".lowercase()

        /** The list of available books. */
        val BOOKS: List<BookData> =
            listOf(
                BookData(
                    cmd = "rules",
                    pages =
                        listOf(
                            // Page 1: Player Rules (1-7)
                            listOf(
                                "<b><u><dark_aqua>Player Rules:<reset>",
                                "",
                                "<gold>▶ <dark_aqua>01 <dark_gray>| <red>No Griefing",
                                "<gold>▶ <dark_aqua>02 <dark_gray>| <red>No Spamming",
                                "<gold>▶ <dark_aqua>03 <dark_gray>| <red>No Advertising",
                                "<gold>▶ <dark_aqua>04 <dark_gray>| <red>No Cursing/No Constant Cursing",
                                "<gold>▶ <dark_aqua>05 <dark_gray>| <red>No Trolling/Flaming",
                                "<gold>▶ <dark_aqua>06 <dark_gray>| <red>No Asking for OP, Ranks, or Items",
                                "<gold>▶ <dark_aqua>07 <dark_gray>| <red>Respect all Players",
                            ),
                            // Page 2: Player Rules (8-13)
                            listOf(
                                "<gold>▶ <dark_aqua>08 <dark_gray>| <red>Obey Staff they are the Law Enforcers",
                                "<gold>▶ <dark_aqua>09 <dark_gray>| <red>No Racist or Sexist Remarks",
                                "<gold>▶ <dark_aqua>10 <dark_gray>| <red>No Mods/Hacks",
                                "<gold>▶ <dark_aqua>12 <dark_gray>| <red>No 1x1 Towers",
                                "<gold>▶ <dark_aqua>13 <dark_gray>| <red>Build in (Fantasy)Medieval style",
                            ),
                            // Page 3: Mod/Admin Rules
                            listOf(
                                "<b><u><dark_aqua>Mod/Admin Rules:<reset>",
                                "",
                                "<gold>▶ <dark_aqua>01 <dark_gray>| " +
                                    "<red>Be Responsible with the power you are given as staff",
                                "<gold>▶ <dark_aqua>02 <dark_gray>| " +
                                    "<red>Do not spawn blocks or items for other players",
                                "<gold>▶ <dark_aqua>03 <dark_gray>| <red>When Trading, only buy and sell legit items",
                                "<gold>▶ <dark_aqua>05 <dark_gray>| <red>No Power Abuse",
                            ),
                        ),
                ),
                BookData(
                    cmd = "guide",
                    pages =
                        listOf(
                            // Page 1: Welcome / Table of Contents
                            listOf(
                                "<b><u><gradient:#832466:#BF4299>VanillaPlus Guide</gradient></b></u>",
                                "",
                                "<dark_gray>Welcome to VanillaPlus!",
                                "",
                                "<gold>📖 <yellow>Contents:",
                                "<gray>▶ <aqua>Page 2: <white>Tool Enchantments",
                                "<gray>▶ <aqua>Page 3: <white>Armor Enchantments",
                                "<gray>▶ <aqua>Page 4: <white>Spell Enchantments",
                                "<gray>▶ <aqua>Page 5: <white>XP System",
                                "<gray>▶ <aqua>Page 6: <white>Custom Recipes",
                                "<gray>▶ <aqua>Page 7: <white>Other Features",
                            ),
                            // Page 2: Tool Enchantments
                            listOf(
                                "<b><u><green>Tool Enchantments</green></u></b>",
                                "",
                                "<gold>🌿 <green>Verdance <dark_gray>(Hoes)",
                                "<gray>  Instantly harvest and replant crops",
                                "",
                                "<gold>🔗 <dark_aqua>Tether <dark_gray>(Tools/Weapons)",
                                "<gray>  Teleports drops and XP directly to you",
                                "",
                                "<gold>⛏️ <dark_red>Earthrend <dark_gray>(Pickaxes)",
                                "<gray>  Mines blocks in a 3x3 area",
                            ),
                            // Page 3: Armor Enchantments
                            listOf(
                                "<b><u><blue>Armor Enchantments</blue></u></b>",
                                "",
                                "<gold>☁️ <aqua>Nimbus <dark_gray>(Happy Ghast)",
                                "<gray>  Allows riding Happy Ghasts with a saddle",
                                "",
                                "<gold>🔥 <red>Embertread <dark_gray>(Boots)",
                                "<gray>  Converts cobblestone paths to stone bricks",
                                "",
                                "<gold>🪶 <yellow>Feather Falling <dark_gray>(Boots)",
                                "<gray>  Reduces fall damage significantly",
                            ),
                            // Page 4: Spell Enchantments
                            listOf(
                                "<b><u><dark_purple>Spell Enchantments</dark_purple></u></b>",
                                "<dark_gray>(Blaze Rod Wands)",
                                "",
                                "<gold>🔥 <red>Inferno <dark_gray>- Fire spell",
                                "<gold>⛈️ <aqua>Skysunder <dark_gray>- Lightning spell",
                                "<gold>💀 <dark_gray>Witherbrand <dark_gray>- Wither spell",
                                "<gold>❄️ <aqua>Frostbind <dark_gray>- Freeze spell",
                                "<gold>🌪️ <gray>Tempest <dark_gray>- Wind spell",
                                "<gold>🌀 <dark_purple>Voidpull <dark_gray>- Pull spell",
                                "<gold>💥 <red>Quake <dark_gray>- Earthquake spell",
                                "",
                                "<gray>Left-click to cast, right-click to cycle",
                            ),
                            // Page 5: XP System
                            listOf(
                                "<b><u><green>XP System</green></u></b>",
                                "",
                                "<gray>Spells cost XP to cast.",
                                "<gray>Each spell has a different cost.",
                                "",
                                "<gold>💰 <yellow>XP Cost Examples:",
                                "<gray>  Inferno: 5 XP",
                                "<gray>  Skysunder: 8 XP",
                                "<gray>  Witherbrand: 10 XP",
                                "<gray>  Frostbind: 6 XP",
                                "",
                                "<gray>Creative mode = free casting!",
                                "<gray>Not enough XP? You'll hear a sound.",
                            ),
                            // Page 6: Custom Recipes
                            listOf(
                                "<b><u><yellow>Custom Recipes</yellow></u></b>",
                                "",
                                "<gold>⛓️ <gray>Chainmail Armor",
                                "<gray>  Craftable from chains",
                                "",
                                "<gold>💎 <aqua>Diamond Recycling",
                                "<gray>  Break down diamond gear",
                                "",
                                "<gold>🎨 <yellow>Painting Variants",
                                "<gray>  New painting recipes",
                                "",
                                "<gold>🍖 <green>Rotten Flesh",
                                "<gray>  Convert to leather",
                                "",
                                "<gold>🪵 <color:#8B4513>Universal Wood Logs",
                                "<gray>  Any wood type crafting",
                            ),
                            // Page 7: Other Features
                            listOf(
                                "<b><u><dark_aqua>Other Features</dark_aqua></u></b>",
                                "",
                                "<gold>💬 <green>Chat Module",
                                "<gray>  Custom chat formatting",
                                "",
                                "<gold>📚 <color:#8B4513>Chiseled Bookshelves",
                                "<gray>  Enhanced bookshelf mechanics",
                                "",
                                "<gold>🎯 <yellow>Locator",
                                "<gray>  Find structure locations",
                                "",
                                "<gold>🪑 <color:#8B4513>Sitting",
                                "<gray>  Sit on stairs and slabs",
                                "",
                                "<gold>📊 <aqua>Scoreboard",
                                "<gray>  Toggleable info display",
                            ),
                        ),
                ),
            )
    }
}
