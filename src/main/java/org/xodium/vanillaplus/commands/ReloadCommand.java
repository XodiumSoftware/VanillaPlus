package org.xodium.vanillaplus.commands;

import java.util.List;

import org.bukkit.entity.Player;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.interfaces.MSG;
import org.xodium.vanillaplus.interfaces.PERMS;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ReloadCommand implements MSG {
    private final static VanillaPlus vp = VanillaPlus.getInstance();

    public static void init(LifecycleEventManager<org.bukkit.plugin.Plugin> man) {
        man.registerEventHandler(LifecycleEvents.COMMANDS, e -> {
            e.registrar().register(
                    Commands.literal("vanillaplus")
                            .executes(ctx -> {
                                if (!(ctx.getSource().getSender() instanceof Player p)) {
                                    vp.getLogger().warning("This command can only be executed by a player!");
                                    return 0;
                                }
                                MiniMessage mm = MiniMessage.miniMessage();
                                if (!p.hasPermission(PERMS.RELOAD)) {
                                    p.sendMessage(
                                            mm.deserialize(
                                                    PREFIX + "<red>You do not have permission to use this command!"));
                                    return 0;
                                }
                                vp.reloadConfig();
                                p.sendMessage(mm.deserialize(PREFIX + "<green>Configuration reloaded successfully."));
                                vp.getLogger().info("Configuration reloaded successfully.");
                                return Command.SINGLE_SUCCESS;
                            })
                            .build(),
                    "Reloads VanillaPlus",
                    List.of("vp"));
        });
    }
}
