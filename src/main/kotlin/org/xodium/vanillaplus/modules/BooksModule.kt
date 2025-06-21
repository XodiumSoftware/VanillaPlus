/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.utils.Utils

/** Represents a module handling book mechanics within the system. */
class BooksModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigManager.data.booksModule.enabled

    private val permPrefix: String = "${instance::class.simpleName}.book".lowercase()

    @Suppress("UnstableApiUsage")
    override fun cmds(): CommandData? {
        return CommandData(
            ConfigManager.data.booksModule.books.map { book ->
                Commands.literal(book.cmd.lowercase())
                    .requires { it.sender.hasPermission("$permPrefix.${book.cmd.lowercase()}") }
                    .executes { ctx -> Utils.tryCatch(ctx) { (ctx.source.sender as Player).openBook(book.toBook()) } }
            },
            "Provides commands to open predefined books.",
            emptyList()
        )
    }

    override fun perms(): List<Permission> {
        return ConfigManager.data.booksModule.books.map {
            Permission(
                "$permPrefix.${it.cmd.lowercase()}",
                "Allows use of the book command: ${it.cmd}",
                PermissionDefault.TRUE
            )
        }
    }
}
