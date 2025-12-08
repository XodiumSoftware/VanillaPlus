package org.xodium.vanillaplus.utils

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.prefix

internal object CommandUtils {
    /**
     * Registers a command execution handler with an automatic try/catch handling.
     * @receiver The command builder this handler is attached to.
     * @param action The action executed when the command runs.
     * @return The same command builder for further configuration.
     */
    fun <T : ArgumentBuilder<CommandSourceStack, T>> T.executesCatching(action: (CommandContext<CommandSourceStack>) -> Unit): T {
        executes { ctx ->
            runCatching { action(ctx) }
                .onFailure { e ->
                    instance.logger.severe(
                        """
                        Command error: ${e.message}
                        ${e.stackTraceToString()}
                        """.trimIndent(),
                    )
                    (ctx.source.sender as? Player)?.sendMessage(
                        "${instance.prefix} <red>An error has occurred. Check server logs for details.".mm(),
                    )
                }
            Command.SINGLE_SUCCESS
        }
        return this
    }
}
