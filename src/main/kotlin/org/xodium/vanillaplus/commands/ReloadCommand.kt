@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

object ReloadCommand {
    private val VP = instance
    private val MM = MiniMessage.miniMessage()

    init {
        VP.lifecycleManager.registerEventHandler(
            LifecycleEvents.COMMANDS
        ) { e ->
            e.registrar().register(
                Commands.literal("vanillaplus")
                    .executes(Command { ctx: CommandContext<CommandSourceStack?>? ->
                        val cs = ctx!!.source!!.sender
                        if (cs is Player) {
                            if (!cs.hasPermission(PERMS.RELOAD)) {
                                cs.sendMessage(MSG.PERM_ERR)
                                return@Command 0
                            }
                        }
                        VP.reloadConfig()
                        cs.sendMessage(MSG.RELOAD_SUCC_MSG)
                        VP.logger.info(MSG.RELOAD_SUCC_LOG_MSG)
                        Command.SINGLE_SUCCESS
                    })
                    .build(),
                "Opens the GUI",
                mutableListOf("vp")
            )
        }
    }

    private interface MSG {
        companion object {
            const val PREFIX: String = "<gold>[<dark_aqua>VanillaPlus<gold>] <reset>"
            val PERM_ERR: Component = MM.deserialize(
                VanillaPlus.PREFIX
                        + "<red>You do not have permission to use this command!"
            )
            val RELOAD_SUCC_MSG: Component = MM
                .deserialize("$PREFIX<green>Configuration reloaded successfully.")
            const val RELOAD_SUCC_LOG_MSG: String = "Configuration reloaded successfully."
        }
    }

    private interface PERMS {
        companion object {
            val RELOAD: String = VP.javaClass.getSimpleName() + ".reload"
        }
    }
}
