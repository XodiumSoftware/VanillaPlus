package org.xodium.vanillaplus.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.interfaces.MSG;
import org.xodium.vanillaplus.interfaces.PERMS;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ReloadCommand implements MSG {
    private static final String RELOAD_SUCC_LOG_MSG = "Configuration reloaded successfully.";
    private static final String RELOAD_SUCC_MSG = PREFIX + "<green>Configuration reloaded successfully.";
    private static final String PERM_ERR_MSG = PREFIX + "<red>You do not have permission to use this command!";
    private final static VanillaPlus vp = VanillaPlus.getInstance();
    private final static MiniMessage mm = MiniMessage.miniMessage();

    {
        vp.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, e -> {
            e.registrar().register(
                    Commands.literal("vanillaplus")
                            .executes(ctx -> {
                                CommandSender cs = ctx.getSource().getSender();
                                if (cs instanceof Player p && !p.hasPermission(PERMS.RELOAD)) {
                                    p.sendMessage(mm.deserialize(PERM_ERR_MSG));
                                    return 0;
                                }
                                vp.reloadConfig();
                                cs.sendMessage(mm.deserialize(RELOAD_SUCC_MSG));
                                vp.getLogger().info(RELOAD_SUCC_LOG_MSG);
                                return Command.SINGLE_SUCCESS;
                            })
                            .build(),
                    "Reloads VanillaPlus",
                    List.of("vp"));
        });
    }
}
