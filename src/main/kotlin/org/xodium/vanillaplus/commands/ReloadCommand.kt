package org.xodium.vanillaplus.commands;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.xodium.vanillaplus.VanillaPlus;

import java.util.List;

public class ReloadCommand {
    private static final VanillaPlus VP = VanillaPlus.getInstance();
    private static final MiniMessage MM = MiniMessage.miniMessage();

    static {
        VP.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, e -> e.registrar().register(
                Commands.literal("vanillaplus")
                        .executes(ctx -> {
                            CommandSender cs = ctx.getSource().getSender();
                            if (cs instanceof Player p) {
                                if (!p.hasPermission(PERMS.RELOAD)) {
                                    p.sendMessage(MSG.PERM_ERR);
                                    return 0;
                                }
                            }
                            VP.reloadConfig();
                            cs.sendMessage(MSG.RELOAD_SUCC_MSG);
                            VP.getLogger().info(MSG.RELOAD_SUCC_LOG_MSG);
                            return Command.SINGLE_SUCCESS;
                        })
                        .build(),
                "Opens the GUI",
                List.of("vp")));
    }

    private interface MSG {
        String PREFIX = "<gold>[<dark_aqua>VanillaPlus<gold>] <reset>";
        @NotNull
        Component PERM_ERR = MM.deserialize(VanillaPlus.PREFIX
                + "<red>You do not have permission to use this command!");
        @NotNull
        Component RELOAD_SUCC_MSG = MM
                .deserialize(PREFIX + "<green>Configuration reloaded successfully.");
        String RELOAD_SUCC_LOG_MSG = "Configuration reloaded successfully.";
    }

    private interface PERMS {
        String RELOAD = VP.getClass().getSimpleName() + ".reload";
    }
}
