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
import net.kyori.adventure.text.Component
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
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
            "Rules".mm(), //TODO: based on config
            instance::class.simpleName.toString().mm(), //TODO: based on config?
            setOf<Component>() //TODO: based on config
        )
    }
}