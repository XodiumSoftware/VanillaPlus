@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus
import org.xodium.vanillaplus.VanillaPlus.Companion.instance


/**
 * The `ReloadCommand` object represents a command handler for reloading the plugin configuration.
 * This command is registered during the plugin's lifecycle and can be executed by players
 * or the console to reload the plugin's configuration without restarting the server.
 *
 * The command only allows players with the appropriate permissions to execute the reload.
 * Upon successful execution, it notifies the sender with a confirmation message and logs
 * the reload action to the server console.
 *
 * Permissions:
 * - Requires the `<class_name_placeholder>.reload` permission to execute the reload command.
 */
object ReloadCommand {
    private val pcn = instance.javaClass.simpleName
    private val MM = MiniMessage.miniMessage()

    private object MSG {
        const val PREFIX = "<gold>[<dark_aqua>VanillaPlus<gold>] <reset>"
        val PERM_ERR = MM.deserialize(
            VanillaPlus.PREFIX
                    + "<red>You do not have permission to use this command!"
        )
        val RELOAD_SUCC_MSG = MM
            .deserialize("$PREFIX<green>Configuration reloaded successfully.")
        const val RELOAD_SUCC_LOG_MSG = "Configuration reloaded successfully."
    }

    init {
        instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(
                Commands.literal("vanillaplus")
                    .executes(Command { ctx: CommandContext<CommandSourceStack?>? ->
                        val cs = ctx?.source?.sender ?: return@Command 0
                        if (cs is Player && !cs.hasPermission("$pcn.reload")) {
                            cs.sendMessage(MSG.PERM_ERR)
                            return@Command 0
                        }
                        instance.reloadConfig()
                        cs.sendMessage(MSG.RELOAD_SUCC_MSG)
                        instance.logger.info(MSG.RELOAD_SUCC_LOG_MSG)
                        Command.SINGLE_SUCCESS
                    })
                    .build(),
                "Opens the GUI",
                mutableListOf("vp")
            )
        }
    }
}
