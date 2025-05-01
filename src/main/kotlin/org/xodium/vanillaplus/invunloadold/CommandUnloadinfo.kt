/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package de.jeff_media.InvUnload

import org.apache.commons.lang.StringUtils
import org.bukkit.block.Block
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.xodium.vanillaplus.invunloadold.Main

class CommandUnloadinfo internal constructor(val main: Main) : CommandExecutor {
    override fun onCommand(
        sender: CommandSender, command: Command, label: String,
        args: Array<String>
    ): Boolean {
        if (sender !is Player) {
            if (args.size == 0) {
                sender.sendMessage("Error: On Console, you must specify a player as parameter.")
                return true
            }
            val p = main.server.getPlayer(args[0])
            if (p == null) {
                sender.sendMessage("Error: Player " + args[0] + " not found.")
                return true
            }
            if (main.visualizer.unloadSummaries.containsKey(p.uniqueId)) {
                val summary: UnloadSummary? = main.visualizer.unloadSummaries.get(p.uniqueId)
                if (summary != null) {
                    summary.print(PrintRecipient.CONSOLE, p)
                    return true
                }
            }
            sender.sendMessage("Player " + p.name + " did not unload or dump their inventory.")
            return true
        }

        val p = sender

        var duration = main.getConfig().getInt("laser-default-duration")
        if (args.size > 0 && StringUtils.isNumeric(args[0])) {
            duration = args[0].toInt()
            if (duration > main.getConfig().getInt("laser-max-duration")) {
                duration = main.getConfig().getInt("laser-max-duration")
            }
        }

        val affectedChests: ArrayList<Block?>? = main.visualizer.lastUnloads.get(p.uniqueId)
        if (affectedChests == null || affectedChests.size == 0) {
            return true
        }
        /*ArrayList<Laser> lasers = main.visualizer.activeLasers.get(p.getUniqueId());
		if(lasers != null) {
			for(Laser laser : lasers) {
				if(laser.isStarted()) laser.stop();
			}
		}*/
        if (main.visualizer.unloadSummaries.containsKey(p.uniqueId)) {
            val summary: UnloadSummary? = main.visualizer.unloadSummaries.get(p.uniqueId)
            if (summary != null) {
                summary.print(PrintRecipient.PLAYER, p)
            }
        }
        //main.visualizer.activeLasers.remove(p.getUniqueId());
        for (block in affectedChests) {
            main.visualizer.chestAnimation(block, p)
        }

        val newVisualizer = false

        if (newVisualizer) {
            //main.visualizer.playLaser(affectedChests,p,duration);
        } else {
            /*if(main.visualizer.activeVisualizations.containsKey(p.getUniqueId())) {
				//p.sendMessage("Experimental: Laser stopped");
				main.visualizer.cancelVisualization(p);
			} else {*/
            //p.sendMessage("Experimental: Laser started");
            main.visualizer.play(p)
        }


        return true
    }
}
