/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.interfaces

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.event.Listener


/**
 * Represents a contract for a module within the system.
 * Every implementing module must define initialization logic and state management.
 * This interface extends the Listener interface, allowing modules to handle events.
 */
interface ModuleInterface : Listener {
    /**
     * Determines if this module is currently enabled.
     *
     * @return `true` if the module is enabled, `false` otherwise.
     */
    fun enabled(): Boolean

    /**
     * Returns the command for this module.
     * If the module does not have a command, this method should return `null`.
     */
    @Suppress("UnstableApiUsage")
    fun cmd(): LiteralArgumentBuilder<CommandSourceStack>? = null
}
