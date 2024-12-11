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

/**
 * The ReloadCommand class handles the registration and execution of the
 * "vanillaplus" command, which opens the settings GUI for players with the
 * appropriate permissions.
 * 
 * <p>
 * This command can only be executed by players. If a player does not have
 * the required permission, they will receive an error message.
 * </p>
 * 
 * <p>
 * Constants:
 * </p>
 * <ul>
 * <li>{@code VP} - An instance of the VanillaPlus plugin.</li>
 * <li>{@code MM} - An instance of the MiniMessage parser.</li>
 * </ul>
 * 
 * <p>
 * Interfaces:
 * </p>
 * <ul>
 * <li>{@code MSG} - Contains pre-defined messages for permission errors and
 * player-only command errors.</li>
 * <li>{@code PERMS} - Contains the permission string required to execute the
 * reload command.</li>
 * </ul>
 * 
 * <p>
 * Static Initialization Block:
 * </p>
 * <ul>
 * <li>Registers the "vanillaplus" command with the command registrar during
 * the plugin's lifecycle events.</li>
 * <li>Checks if the command sender is a player and has the required
 * permission before opening the settings GUI.</li>
 * <li>Sends appropriate error messages if the sender is not a player or
 * lacks the required permission.</li>
 * </ul>
 */
public class ReloadCommand {
    private static final VanillaPlus VP = VanillaPlus.getInstance();
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static interface MSG {
        @NotNull
        Component PERM_ERR = MM.deserialize(VanillaPlus.PREFIX
                + "<red>You do not have permission to use this command!");
        @NotNull
        Component PLAYER_ONLY_CMD = MM
                .deserialize("This command can only be run by a player.");
    }

    private static interface PERMS {
        String RELOAD = VP.getClass().getSimpleName() + ".reload";
    }

    static {
        VP.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, e -> {
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
