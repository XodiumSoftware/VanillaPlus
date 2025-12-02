package org.xodium.vanillaplus.interfaces

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.event.Listener
import org.bukkit.permissions.Permission
import org.xodium.vanillaplus.VanillaPlus.Companion.configData
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.ConfigData
import kotlin.time.measureTime

/** Represents a contract for a feature within the system. */
internal interface FeatureInterface : Listener {
    /** Registers this feature as an event listener with the server. */
    @Suppress("UnstableApiUsage")
    fun register() {
        instance.logger.info(
            "Registering: ${this::class.simpleName} | Took ${
                measureTime {
                    instance.server.pluginManager.registerEvents(this, instance)
                    instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
                        cmds.forEach { cmd ->
                            event.registrar().register(
                                cmd.builder.build(),
                                cmd.description,
                                cmd.aliases,
                            )
                        }
                    }
                    instance.server.pluginManager.addPermissions(perms)
                }.inWholeMilliseconds
            }ms",
        )
    }

    /**
     * Retrieves the configuration data associated with the module.
     * @return A [ConfigData] object representing the configuration for the module.
     */
    val config: ConfigData
        get() = configData

    /**
     * Retrieves a list of command data associated with the module.
     * @return A list of [CommandData] objects representing the commands for the module.
     */
    val cmds: List<CommandData>
        get() = emptyList()

    /**
     * Retrieves a list of permissions associated with this module.
     * @return A [List] of [Permission] objects representing the permissions for this module.
     */
    val perms: List<Permission>
        get() = emptyList()
}
