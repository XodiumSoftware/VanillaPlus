package org.xodium.vanillaplus.commands;

import java.util.List;

import org.bukkit.entity.Player;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.interfaces.PERMS;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ReloadCommand {
    private final static VanillaPlus plugin = VanillaPlus.getInstance();

    public static void init(LifecycleEventManager<org.bukkit.plugin.Plugin> manager) {
        manager.registerEventHandler(LifecycleEvents.COMMANDS, e -> {
            e.registrar().register(
                    Commands.literal("vanillaplus")
                            .executes(ctx -> {
                                if (!(ctx.getSource().getSender() instanceof Player player)) {
                                    plugin.getLogger().warning("This command can only be executed by a player!");
                                    return 0;
                                }
                                MiniMessage mm = MiniMessage.miniMessage();
                                if (!player.hasPermission(PERMS.RELOAD)) {
                                    player.sendMessage(
                                            mm.deserialize("<red>You do not have permission to use this command!"));
                                    return 0;
                                }
                                return Command.SINGLE_SUCCESS;
                            })
                            .build(),
                    "Reloads VanillaPlus",
                    List.of("vp"));
        });
    }
}
