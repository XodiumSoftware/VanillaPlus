package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.executesCatching

/** Represents a module handling book mechanics within the system. */
internal object BooksModule : ModuleInterface {
    private val permPrefix: String = "${instance.javaClass.simpleName}.book".lowercase()

    override val cmds =
        config.booksModule.books.map { book ->
            CommandData(
                Commands
                    .literal(book.cmd.lowercase())
                    .requires { it.sender.hasPermission("$permPrefix.${book.cmd.lowercase()}") }
                    .executesCatching {
                        if (it.source.sender !is Player) {
                            instance.logger.warning("Command can only be executed by a Player!")
                        }
                        it.source.sender.openBook(book.toBook())
                    },
                "Opens the predefined book '${book.cmd.lowercase()}'",
                emptyList(),
            )
        }

    override val perms =
        config.booksModule.books.map {
            Permission(
                "$permPrefix.${it.cmd.lowercase()}",
                "Allows use of the book command: ${it.cmd}",
                it.permission,
            )
        }
}
