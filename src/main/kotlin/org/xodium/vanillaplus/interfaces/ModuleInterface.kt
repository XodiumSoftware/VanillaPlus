package org.xodium.vanillaplus.interfaces

import org.bukkit.event.Listener
import org.bukkit.permissions.Permission
import org.xodium.vanillaplus.data.CommandData

/** Represents a contract for a module within the system. */
internal interface ModuleInterface : Listener {
    /**
     * Retrieves a list of command data associated with the module.
     * @return A list of [CommandData] objects representing the commands for the module.
     */
    fun cmds(): List<CommandData> = emptyList()

    /**
     * Retrieves a list of permissions associated with this module.
     * @return A [List] of [Permission] objects representing the permissions for this module.
     */
    fun perms(): List<Permission> = emptyList()
}
