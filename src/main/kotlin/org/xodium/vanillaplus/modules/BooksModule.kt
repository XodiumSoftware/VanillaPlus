/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.inventory.Book
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.Utils.fireFmt
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.interfaces.ModuleInterface

/**
 * TODO: Add description
 */
class BooksModule : ModuleInterface {
    override fun enabled(): Boolean = Config.BooksModule.ENABLED

    @Suppress("UnstableApiUsage")
    override fun cmd(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("rules")
            .requires { it.sender.hasPermission(Perms.AutoRefill.USE) }
            .executes(Command { Utils.tryCatch(it) { book() } })
    }


    /**
     * @return the book for the module
     */
    private fun book(): Book {
        return Book.book(
            Config.BooksModule.BOOK.title.fireFmt().mm(),
            Config.BooksModule.BOOK.author.fireFmt().mm(),
            Config.BooksModule.BOOK.pages.map { it.mm() }.toSet()
        )
    }
}




