package org.xodium.vanillaplus.interfaces

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.event.Listener
import org.bukkit.permissions.Permission
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import kotlin.time.measureTime

/** Represents a contract for a feature within the system. */
internal interface FeatureInterface :
    DataInterface,
    Listener {
    /** Registers this feature as an event listener with the server. */
    @Suppress("UnstableApiUsage")
    fun register() {
        instance.logger.info(
            "Registering: ${this::class.simpleName} | Took ${
                measureTime {
                    DataInterface.registerFeature(this)
                    load()
                    instance.server.pluginManager.registerEvents(this, instance)
                    instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
                        cmds().forEach { cmd ->
                            event.registrar().register(
                                cmd.builder.build(),
                                cmd.description,
                                cmd.aliases,
                            )
                        }
                    }
                    instance.server.pluginManager.addPermissions(perms())
                }.inWholeMilliseconds
            }ms",
        )
    }

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
