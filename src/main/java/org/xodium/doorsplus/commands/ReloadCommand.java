package org.xodium.doorsplus.commands;

import org.xodium.doorsplus.DoorsPlus;
import com.mojang.brigadier.Command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

public class ReloadCommand {
    public static void init(LifecycleEventManager<org.bukkit.plugin.Plugin> manager) {
        manager.registerEventHandler(LifecycleEvents.COMMANDS, e -> {
            e.registrar().register(
                    Commands.literal("doorsplus")
                            .then(Commands.literal("reload")
                                    .executes(ctx -> {
                                        ctx.getSource().getSender()
                                                .sendMessage("Reloading DoorsPlus...");
                                        DoorsPlus.getInstance().reload();
                                        ctx.getSource().getSender().sendMessage("DoorsPlus reloaded!");
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            .build(),
                    "Reloads the DoorsPlus plugin");
        });
    }
}
