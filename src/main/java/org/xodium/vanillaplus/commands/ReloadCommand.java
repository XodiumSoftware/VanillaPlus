package org.xodium.vanillaplus.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.gui.SettingsGUI;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ReloadCommand {
    private static final VanillaPlus vp = VanillaPlus.getInstance();
    private static final MiniMessage mm = MiniMessage.miniMessage();

    private interface MSG {
        @NotNull
        Component PERM_ERR = mm.deserialize(VanillaPlus.PREFIX
                + "<red>You do not have permission to use this command!");
        @NotNull
        Component PLAYER_ONLY_CMD = mm
                .deserialize("This command can only be run by a player.");
    }

    private interface PERMS {
        String RELOAD = vp.getClass().getSimpleName() + ".reload";
    }

    {
        vp.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, e -> {
            e.registrar().register(
                    Commands.literal("vanillaplus")
                            .executes(ctx -> {
                                CommandSender cs = ctx.getSource().getSender();
                                if (cs instanceof Player p) {
                                    if (!p.hasPermission(PERMS.RELOAD)) {
                                        p.sendMessage(MSG.PERM_ERR);
                                        return 0;
                                    }
                                    SettingsGUI.openInventory(p);
                                    return Command.SINGLE_SUCCESS;
                                }
                                cs.sendMessage(MSG.PLAYER_ONLY_CMD);
                                return 0;
                            })
                            .build(),
                    "Opens the GUI",
                    List.of("vp"));
        });
    }
}
