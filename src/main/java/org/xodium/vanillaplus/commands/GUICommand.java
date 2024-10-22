package org.xodium.vanillaplus.commands;

import com.mojang.brigadier.Command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

public class GUICommand {
    public static void init(LifecycleEventManager<org.bukkit.plugin.Plugin> manager) {
        manager.registerEventHandler(LifecycleEvents.COMMANDS, e -> {
            e.registrar().register(
                    Commands.literal("doorsplus")
                            .executes(ctx -> {
                                ctx.getSource().getSender().sendMessage("Opening DoorsPlus GUI...");
                                // TODO: make this open the GUI
                                ctx.getSource().getSender().sendMessage("DoorsPlus GUI opened!");
                                return Command.SINGLE_SUCCESS;
                            })
                            .build(),
                    "Opens the DoorsPlus GUI");
        });
    }
}
