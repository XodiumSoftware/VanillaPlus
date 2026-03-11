package org.xodium.vanillaplus.interfaces

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.event.Listener
import org.bukkit.permissions.Permission
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import kotlin.time.measureTime

/** Represents a contract for a module within the system. */
internal interface ModuleInterface : Listener {
    /**
     * Retrieves the module-specific configuration.
     * @return A [ModuleConfigInterface] representing the configuration for this module.
     */
    val config: ModuleConfigInterface

    /**
     * Retrieves a list of command data associated with the module.
     * @return A [Collection] of [CommandData] objects representing the commands for the module.
     */
    val cmds: Collection<CommandData> get() = emptyList()

    /**
     * Retrieves a list of permissions associated with this module.
     * @return A [List] of [Permission] objects representing the permissions for this module.
     */
    val perms: List<Permission> get() = emptyList()

    /**
     * Registers this feature as an event listener with the server.
     * @return The time taken to register the feature in milliseconds, or null if the feature is disabled.
     */
    @Suppress("UnstableApiUsage")
    fun register(): Long =
        measureTime {
            instance.server.pluginManager.registerEvents(this, instance)
            instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
                cmds.forEach { cmd ->
                    it.registrar().register(
                        cmd.builder.build(),
                        cmd.description,
                        cmd.aliases,
                    )
                }
            }
            instance.server.pluginManager.addPermissions(perms)
        }.inWholeMilliseconds
}
