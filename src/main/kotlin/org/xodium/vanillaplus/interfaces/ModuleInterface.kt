/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.interfaces

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.event.Listener
import org.bukkit.permissions.Permission

/** Represents a contract for a module within the system. */
interface ModuleInterface : Listener {
    /**
     * Determines if this module is currently enabled.
     * @return `true` if the module is enabled, `false` otherwise.
     */
    fun enabled(): Boolean

    /**
     * Constructs and returns a [Collection] of [LiteralArgumentBuilder]s for the current [CommandSourceStack].
     * @return A [Collection] of [LiteralArgumentBuilder] instances representing the command structures, or `null` if not applicable.
     */
    @Suppress("UnstableApiUsage")
    fun cmds(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? = null

    /**
     * Retrieves a list of permissions associated with this module.
     * @return A [List] of [Permission] objects representing the permissions for this module.
     */
    fun perms(): List<Permission> = emptyList()
}
