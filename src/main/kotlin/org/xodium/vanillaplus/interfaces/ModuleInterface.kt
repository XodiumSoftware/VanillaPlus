/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.interfaces

import org.bukkit.event.Listener
import org.bukkit.permissions.Permission
import org.xodium.vanillaplus.data.CommandData

/** Represents a contract for a module within the system. */
interface ModuleInterface : Listener {
    /**
     * Determines if this module is currently enabled.
     * @return `true` if the module is enabled, `false` otherwise.
     */
    fun enabled(): Boolean

    /**
     * Retrieves the command data for this module.
     * @return A [CommandData] object containing commands, description, and aliases.
     */
    fun cmds(): CommandData? = null

    /**
     * Retrieves a list of permissions associated with this module.
     * @return A [List] of [Permission] objects representing the permissions for this module.
     */
    fun perms(): List<Permission> = emptyList()
}
