/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package org.xodium.vanillaplus.invunloadold

import org.bukkit.block.Block
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandUnloadInfo internal constructor(val main: Main) : CommandExecutor {
    override fun onCommand(
        sender: CommandSender, command: Command, label: String,
        args: Array<String>
    ): Boolean {
        if (sender !is Player) {
            if (args.isEmpty()) {
                sender.sendMessage("Error: On Console, you must specify a player as parameter.")
                return true
            }
            val p = main.server.getPlayer(args[0])
            if (p == null) {
                sender.sendMessage("Error: Player " + args[0] + " not found.")
                return true
            }
            if (Visualizer().unloadSummaries.containsKey(p.uniqueId)) {
                val summary: UnloadSummary? = Visualizer().unloadSummaries[p.uniqueId]
                if (summary != null) {
                    summary.print(UnloadSummary.PrintRecipient.CONSOLE, p)
                    return true
                }
            }
            sender.sendMessage("Player " + p.name + " did not unload or dump their inventory.")
            return true
        }

        val p = sender
        val affectedChests: ArrayList<Block?>? = Visualizer() lastUnloads [p.uniqueId]
        if (affectedChests == null || affectedChests.isEmpty()) {
            return true
        }
        if (Visualizer().unloadSummaries.containsKey(p.uniqueId)) {
            val summary: UnloadSummary? = Visualizer().unloadSummaries[p.uniqueId]
            summary?.print(UnloadSummary.PrintRecipient.PLAYER, p)
        }
        for (block in affectedChests) {
            Visualizer().chestAnimation(block, p)
        }
        Visualizer().play(p)
        return true
    }
}
