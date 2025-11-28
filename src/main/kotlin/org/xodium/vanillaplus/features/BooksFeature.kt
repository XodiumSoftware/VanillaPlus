package org.xodium.vanillaplus.features

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.FeatureInterface
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch

/** Represents a feature handling book mechanics within the system. */
internal object BooksFeature : FeatureInterface {
    private val permPrefix: String = "${instance.javaClass.simpleName}.book".lowercase()

    override fun cmds(): List<CommandData> =
        config.booksFeature.books.map { book ->
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
        config.booksFeature.books.map {
            Permission(
                "$permPrefix.${it.cmd.lowercase()}",
                "Allows use of the book command: ${it.cmd}",
                it.permission,
            )
        }
}
