package org.xodium.vanillaplus.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.xodium.vanillaplus.VanillaPlus;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ReloadCommand {
    private static final VanillaPlus vp = VanillaPlus.getInstance();
    private static final PluginManager pm = vp.getServer().getPluginManager();
    private static final String vpcn = vp.getClass().getSimpleName();
    private static final MiniMessage mm = MiniMessage.miniMessage();

    private interface MSG {
        String RELOAD_SUCC_LOG = "Reloaded successfully.";
        String RELOAD_SUCC = VanillaPlus.PREFIX + "<green>Reloaded successfully.";
        String PERM_ERR = VanillaPlus.PREFIX
                + "<red>You do not have permission to use this command!";
    }

    private interface PERMS {
        String RELOAD = vpcn + ".reload";
    }

    {
        vp.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, e -> {
            e.registrar().register(
                    Commands.literal("vanillaplus")
                            .executes(ctx -> {
                                CommandSender cs = ctx.getSource().getSender();
                                if (cs instanceof Player p && !p.hasPermission(PERMS.RELOAD)) {
                                    p.sendMessage(mm.deserialize(MSG.PERM_ERR));
                                    return 0;
                                }
                                pm.disablePlugin(vp);
                                pm.enablePlugin(vp);
                                cs.sendMessage(mm.deserialize(MSG.RELOAD_SUCC));
                                vp.getLogger().info(MSG.RELOAD_SUCC_LOG);
                                return Command.SINGLE_SUCCESS;
                            })
                            .build(),
                    "Reloads VanillaPlus",
                    List.of("vp"));
        });
    }
}
