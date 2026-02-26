package org.xodium.vanillaplus.utils

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.prefix

/** Utility functions for command handling. */
internal object CommandUtils {
    /**
     * Registers a command execution handler with an automatic try/catch handling.
     * @receiver The command builder this handler is attached to.
     * @param action The action executed when the command runs.
     * @return The same command builder for further configuration.
     */
    fun <T : ArgumentBuilder<CommandSourceStack, T>> T.executesCatching(
        action: (CommandContext<CommandSourceStack>) -> Unit,
    ): T {
        executes { ctx ->
            runCatching { action(ctx) }
                .onFailure {
                    instance.logger.severe(
                        """
                        Command error: ${it.message}
                        ${it.stackTraceToString()}
                        """.trimIndent(),
                    )
                    (ctx.source.sender as? Player)?.sendMessage(
                        MM.deserialize("${instance.prefix} <red>An error has occurred. Check server logs for details."),
                    )
                }
            Command.SINGLE_SUCCESS
        }
        return this
    }

    /**
     * Registers a command execution handler specifically for Player senders with an automatic try/catch handling.
     * @receiver The command builder this handler is attached to.
     * @param action The action executed when the command runs, receiving the Player and command context.
     * @return The same command builder for further configuration.
     * @throws IllegalStateException if the command is executed by a non-Player sender.
     */
    fun <T : ArgumentBuilder<CommandSourceStack, T>> T.playerExecuted(
        action: (Player, CommandContext<CommandSourceStack>) -> Unit,
    ): T {
        executesCatching {
            action(
                it.source.sender as? Player ?: run {
                    instance.logger.warning("Command can only be executed by a Player!")
                    return@executesCatching
                },
                it,
            )
        }
        return this
    }
}
