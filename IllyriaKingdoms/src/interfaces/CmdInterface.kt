package org.xodium.illyriaplus.interfaces

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.permissions.Permission
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import org.xodium.illyriaplus.data.CommandData
import kotlin.time.measureTime

/** Represents a contract for a cmd within the system. */
internal interface CmdInterface {
    /**
     * Retrieves a list of command data associated with the mechanic.
     *
     * @return A [Collection] of [CommandData] objects representing the commands for the mechanic.
     */
    val cmds: Collection<CommandData>

    /**
     * Retrieves a list of permissions associated with this mechanic.
     *
     * @return A [List] of [Permission] objects representing the permissions for this mechanic.
     */
    val perms: List<Permission>

    /**
     * Registers this feature with the server.
     *
     * @return The time taken to register the feature in milliseconds.
     */
    @Suppress("UnstableApiUsage")
    fun register(): Long =
        measureTime {
            instance.server.pluginManager.addPermissions(perms)
            instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
                cmds.forEach { cmd ->
                    it.registrar().register(
                        cmd.builder.build(),
                        cmd.description,
                        cmd.aliases,
                    )
                }
            }
        }.inWholeMilliseconds
}
