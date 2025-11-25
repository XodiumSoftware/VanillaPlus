package org.xodium.vanillaplus.features

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.BookData
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.FeatureInterface
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch

/** Represents a feature handling book mechanics within the system. */
internal object BooksFeature : FeatureInterface {
    private val config: Config = Config()
    private val permPrefix: String = "${instance.javaClass.simpleName}.book".lowercase()

    override fun cmds(): List<CommandData> =
        config.books.map { book ->
            CommandData(
                Commands
                    .literal(book.cmd.lowercase())
                    .requires { it.sender.hasPermission("$permPrefix.${book.cmd.lowercase()}") }
                    .executes { ctx ->
                        ctx.tryCatch {
                            if (it.sender !is Player) {
                                instance.logger.warning(
                                    "Command can only be executed by a Player!",
                                )
                            }
                            it.sender.openBook(book.toBook())
                        }
                    },
                "Opens the predefined book '${book.cmd.lowercase()}'",
                emptyList(),
            )
        }

    override fun perms(): List<Permission> =
        config.books.map {
            Permission(
                "$permPrefix.${it.cmd.lowercase()}",
                "Allows use of the book command: ${it.cmd}",
                it.permission,
            )
        }

    data class Config(
        var books: List<BookData> =
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
                                "<gold>▶ <dark_aqua>01 <dark_gray>| <red>Be Responsible with the power you are given as staff",
                                "<gold>▶ <dark_aqua>02 <dark_gray>| <red>Do not spawn blocks or items for other players",
                                "<gold>▶ <dark_aqua>03 <dark_gray>| <red>When Trading, only buy and sell legit items",
                                "<gold>▶ <dark_aqua>05 <dark_gray>| <red>No Power Abuse",
                            ),
                        ),
                ),
            ),
    )
}
