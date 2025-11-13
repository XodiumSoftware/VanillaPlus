package org.xodium.vanillaplus.interfaces

import org.bukkit.event.Listener
import org.bukkit.permissions.Permission
import org.xodium.vanillaplus.data.CommandData

/** Represents a contract for a module within the system. */
internal interface ModuleInterface<out T : ModuleInterface.Config> : Listener {
    /**
     * Represents the configuration settings for a module.
     * @property enabled Indicates whether the module is enabled or not.
     */
    interface Config {
        var enabled: Boolean
            get() = true
            set(_) {}
    }

    /**
     * Retrieves the configuration for this module.
     * @return A [Config] object containing the module's configuration settings.
     */
    val config: T

    /**
     * Determines if this module is currently enabled.
     * @return `true` if the module is enabled, `false` otherwise.
     */
    fun enabled(): Boolean = config.enabled

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
