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

/** Represents a module handling book mechanics within the system. */
internal class BooksModule : ModuleInterface<BooksModule.Config> {
    override val config: Config = Config()

    private val permPrefix: String = "${instance::class.simpleName}.book".lowercase()

    override fun cmds(): List<CommandData> =
        config.books.map { book ->
            CommandData(
                Commands
                    .literal(book.cmd.lowercase())
                    .requires { it.sender.hasPermission("$permPrefix.${book.cmd.lowercase()}") }
                    .executes { ctx -> ctx.tryCatch { (it.sender as Player).openBook(book.toBook()) } },
                "Opens the predefined book '${book.cmd.lowercase()}'.",
                emptyList(),
            )
        }

    override fun perms(): List<Permission> =
        config.books.map {
            Permission(
                "$permPrefix.${it.cmd.lowercase()}",
                "Allows use of the book command: ${it.cmd}",
                PermissionDefault.TRUE,
            )
        }

    data class Config(
        override var enabled: Boolean = true,
        var books: List<BookData> =
            listOf(
                // --- EXAMPLE BOOK (DEMO FORMAT) ---
                BookData(
                    cmd = "example", // Command to open: `/example`
                    title = "<gradient:gold:yellow>Example Book</gradient>", // Fancy title
                    author = instance::class.simpleName.toString(),
                    pages =
                        listOf(
                            // Page 1
                            listOf(
                                "<bold><underlined>Welcome to the Example Book!",
                                "",
                                "<gold>▶</gold> This is a <green>formatted</green> book.",
                                "<gold>▶</gold> Use <white>/example</white> to open it.",
                                "",
                                "<gray>Supports: <bold>MiniMessage</bold> formatting!",
                            ),
                            // Page 2
                            listOf(
                                "<bold><underlined>How to Add More Books",
                                "",
                                "1. Duplicate this config,",
                                "2. Change the <white>cmd</white>, <white>title</white>, and <white>pages</white>.",
                                "",
                                "<red>⚠</red> <gray>Permissions auto-generate!",
                            ),
                        ),
                ),
                // --- Add REAL books below (e.g., "rules", "guide") ---
            ),
    ) : ModuleInterface.Config
}
