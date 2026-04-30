package org.xodium.illyriaplus.interfaces

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.permissions.Permission
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import org.xodium.illyriaplus.data.CommandData
import kotlin.time.measureTime

/** Represents a contract for a command within the system. */
internal interface CmdInterface {
    /**
     * The command data associated with this command.
     *
     * @return A [CommandData] instance describing the command.
     */
    val cmd: CommandData

    /**
     * The permission required for this command.
     *
     * @return A [Permission] instance representing the command permission.
     */
    val perm: Permission

    /**
     * Registers the command and its permission with the server.
     *
     * @return The time taken to complete the registration in milliseconds.
     */
    fun register(): Long =
        measureTime {
            instance.server.pluginManager.addPermission(perm)
            instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
                it.registrar().register(
                    cmd.builder.build(),
                    cmd.description,
                    cmd.aliases,
                )
            }
        }.inWholeMilliseconds
}
