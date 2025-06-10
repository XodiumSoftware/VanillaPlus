/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.utils.Utils

/** Represents a module handling book mechanics within the system. */
class BooksModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigManager.data.booksModule.enabled

    @Suppress("UnstableApiUsage")
    override fun cmds(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("guide")
                .requires { it.sender.hasPermission(Perms.Book.GUIDE) }
                .executes { it -> Utils.tryCatch(it) { (it.sender as Player).openBook(ConfigManager.BooksModule.GUIDE_BOOK) } },
            Commands.literal("rules")
                .requires { it.sender.hasPermission(Perms.Book.RULES) }
                .executes { it -> Utils.tryCatch(it) { (it.sender as Player).openBook(ConfigManager.BooksModule.RULES_BOOK) } })
    }
}




