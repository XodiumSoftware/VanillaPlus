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
import org.xodium.vanillaplus.hooks.ChestSortHook
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils

/** Represents a module handling inv-unload mechanics within the system. */
class InvUnloadModule : ModuleInterface {
    override fun enabled(): Boolean = Config.InvUnloadModule.ENABLED

    @Suppress("UnstableApiUsage")
    override fun cmd(): LiteralArgumentBuilder<CommandSourceStack>? {
        return Commands.literal("invunload")
            .requires { it.sender.hasPermission(Perms.AutoRefill.USE) }
            .executes { it -> Utils.tryCatch(it) { TODO("add unload here") } }
    }

    init {
        if (enabled()) {
            ChestSortHook.sort(TODO())
            ChestSortHook.shouldSort(TODO())
        }
    }
}