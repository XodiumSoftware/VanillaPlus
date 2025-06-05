/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils

/** Represents a module handling map mechanics within the system. */
class MapModule : ModuleInterface {
    override fun enabled(): Boolean = Config.MapModule.ENABLED

    @Suppress("UnstableApiUsage")
    override fun cmds(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("mapify")
                .requires { it.sender.hasPermission(Perms.InvUnload.USE) }
                .executes { it -> Utils.tryCatch(it) { mapify() } }
        )
    }

    private fun mapify() {}
}