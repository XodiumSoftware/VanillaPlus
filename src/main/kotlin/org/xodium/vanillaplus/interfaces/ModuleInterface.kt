package org.xodium.vanillaplus.interfaces

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.event.Listener
import org.bukkit.permissions.Permission
import org.xodium.vanillaplus.VanillaPlus.Companion.configData
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.ConfigData
import java.util.logging.Logger
import kotlin.reflect.full.memberProperties
import kotlin.time.measureTime

/** Represents a contract for a module within the system. */
internal interface ModuleInterface : Listener {
    /**
     * Retrieves the configuration data associated with the module.
     * @return A [ConfigData] object representing the configuration for the module.
     */
    val config: ConfigData get() = configData

    /**
     * Determines if this module is enabled.
     * @return True if the module is enabled, false otherwise.
     */
    val isEnabled: Boolean
        get() =
            configData::class
                .memberProperties
                .firstOrNull { property ->
                    property.name == (this::class.simpleName?.replaceFirstChar { it.lowercase() } ?: return true)
                }?.call(configData)
                ?.let { moduleConfig ->
                    moduleConfig::class
                        .memberProperties
                        .firstOrNull { it.name == "enabled" }
                        ?.call(moduleConfig) as? Boolean
                }
                ?: true

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
    fun register(): Long? {
        if (!isEnabled) return null

        return measureTime {
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
    }

    /**
     * Logs the registration details of a list of modules.
     * @receiver Logger The logger to use for logging.
     * @param modules List of [ModuleInterface] instances to log.
     */
    fun Logger.info(modules: List<ModuleInterface>) {
        info("Registered: ${modules.size} module(s) | Took ${modules.sumOf { it.register() ?: return }}ms")
    }
}
