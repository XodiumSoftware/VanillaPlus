/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.mapify.command;


import org.bukkit.command.PluginCommand;
import org.xodium.vanillaplus.mapify.Mapify;

public class CommandManager {

    public CommandManager(Mapify plugin) {

        PluginCommand commandMapify = plugin.getCommand("mapify");
        commandMapify.setExecutor(new CommandMapify());

        PluginCommand refreshMaps = plugin.getCommand("refreshmaps");
        refreshMaps.setExecutor(new CommandRefreshMaps());
    }

}
