package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.BookData
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.skylineFmt

/** Represents a module handling book mechanics within the system. */
class BooksModule : ModuleInterface<BooksModule.Config> {
    override val config: Config = Config()
    private val permPrefix: String = "${instance::class.simpleName}.book".lowercase()

    override fun enabled(): Boolean = config.enabled

    override fun cmds(): CommandData {
        return CommandData(
            config.books.map { book ->
                Commands.literal(book.cmd.lowercase())
                    .requires { it.sender.hasPermission("$permPrefix.${book.cmd.lowercase()}") }
                    .executes { ctx -> ctx.tryCatch { (it.sender as Player).openBook(book.toBook()) } }
            },
            "Provides commands to open predefined books.",
            emptyList()
        )
    }

    override fun perms(): List<Permission> {
        return config.books.map {
            Permission(
                "$permPrefix.${it.cmd.lowercase()}",
                "Allows use of the book command: ${it.cmd}",
                PermissionDefault.TRUE
            )
        }
    }

    data class Config(
        override var enabled: Boolean = true,
        var books: List<BookData> = listOf(
            BookData(
                "guide",
                "Guide".fireFmt(),
                instance::class.simpleName.toString().fireFmt(),
                listOf(
                    // Page 1
                    listOf(
                        "<b><u>${"Tips & Tricks".fireFmt()}",
                        "",
                        "<gold>▶ ${"/trowel".skylineFmt()}",
                        "<dark_gray>Toggles trowel mode",
                        "<dark_gray>random picks item from hotbar to place",
                        "",
                        "<gold>▶ ${"/invu".skylineFmt()}",
                        "<dark_gray>Unloads your inventory into nearby chests",
                        "",
                        "<gold>▶ ${"/invs".skylineFmt()}",
                        "<dark_gray>Search into nearby chests for an item"
                    ),
                    // Page 2
                    listOf(
                        "<gold>▶ ${"/nick <formatted_name>".skylineFmt()}",
                        "<dark_gray>Change your nickname, Visit: <b>birdflop.com</b>,",
                        "<dark_gray>Color Format: <b>MiniMessage</b>,",
                        "<dark_gray>Copy output into <formatted_name>",
                        "",
                        "<gold>▶ ${"Enchantment max level".skylineFmt()}",
                        "<dark_gray>has been incremented by <red><b>x2"
                    )
                )
            ),
            BookData(
                "rules",
                "Rules".fireFmt(),
                instance::class.simpleName.toString().fireFmt(),
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
                        "<gold>▶ <dark_aqua>07 <dark_gray>| <red>Respect all Players"
                    ),
                    // Page 2: Player Rules (8-13)
                    listOf(
                        "<gold>▶ <dark_aqua>08 <dark_gray>| <red>Obey Staff they are the Law Enforcers",
                        "<gold>▶ <dark_aqua>09 <dark_gray>| <red>No Racist or Sexist Remarks",
                        "<gold>▶ <dark_aqua>10 <dark_gray>| <red>No Mods/Hacks",
                        "<gold>▶ <dark_aqua>11 <dark_gray>| <red>No Full Caps Messages",
                        "<gold>▶ <dark_aqua>12 <dark_gray>| <red>No 1x1 Towers",
                        "<gold>▶ <dark_aqua>13 <dark_gray>| <red>Build in (Fantasy)Medieval style"
                    ),
                    // Page 3: Mod/Admin Rules
                    listOf(
                        "<b><u><dark_aqua>Mod/Admin Rules:<reset>",
                        "",
                        "<gold>▶ <dark_aqua>01 <dark_gray>| <red>Be Responsible with the power you are given as staff",
                        "<gold>▶ <dark_aqua>02 <dark_gray>| <red>Do not spawn blocks or items for other players",
                        "<gold>▶ <dark_aqua>03 <dark_gray>| <red>When Trading, only buy and sell legit items",
                        "<gold>▶ <dark_aqua>05 <dark_gray>| <red>No Power Abuse"
                    )
                )
            )
        ),
    ) : ModuleInterface.Config
}
