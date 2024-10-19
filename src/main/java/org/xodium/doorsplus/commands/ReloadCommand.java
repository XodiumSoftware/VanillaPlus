package org.xodium.doorsplus.commands;

import org.xodium.doorsplus.Main;
import org.xodium.doorsplus.interfaces.MSG;

import com.mojang.brigadier.Command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

public class ReloadCommand {
    private final static Main main = Main.getInstance();

    public static void init(LifecycleEventManager<org.bukkit.plugin.Plugin> manager) {
        manager.registerEventHandler(LifecycleEvents.COMMANDS, e -> {
            final Commands cmds = e.registrar();
            cmds.register(
                    Commands.literal("reload")
                            .executes(ctx -> {
                                ctx.getSource().getSender().sendMessage(MSG.RELOADING);
                                main.reload();
                                ctx.getSource().getSender().sendMessage(MSG.RELOADED);
                                return Command.SINGLE_SUCCESS;
                            })
                            .build(),
                    "Reloads the plugin");
        });
    }

}
