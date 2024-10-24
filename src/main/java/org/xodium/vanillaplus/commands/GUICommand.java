package org.xodium.vanillaplus.commands;

import org.bukkit.entity.Player;
import org.xodium.vanillaplus.listeners.GUIListener;

import com.mojang.brigadier.Command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

public class GUICommand {
    public static void init(LifecycleEventManager<org.bukkit.plugin.Plugin> manager) {
        manager.registerEventHandler(LifecycleEvents.COMMANDS, e -> {
            e.registrar().register(
                    Commands.literal("vanillaplus")
                            .executes(ctx -> {
                                Player p = (Player) ctx.getSource().getSender();
                                GUIListener.openGUI(p);
                                return Command.SINGLE_SUCCESS;
                            })
                            .build(),
                    "Opens the VanillaPlus GUI");
        });
    }
}
