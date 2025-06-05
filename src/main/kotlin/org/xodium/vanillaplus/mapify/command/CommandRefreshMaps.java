/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.mapify.command;

import com.funnyboyroks.mapify.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.Collections;
import java.util.List;

public class CommandRefreshMaps implements CommandExecutor, TabCompleter {

    private ItemStack[] getItemFramesInRadius(Player player, int radius) {
        return player.getNearbyEntities(radius, radius, radius).stream()
                .filter(e -> e instanceof ItemFrame)
                .map(e -> ((ItemFrame) e).getItem())
                .filter(i -> !i.getType().isAir())
                .toArray(ItemStack[]::new);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run in-game.");
            return true;
        }

        if (!sender.hasPermission("mapify.command.refreshmaps")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to run this command.");
            return true;
        }

        if (args.length > 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /refreshmaps [radius]");
            return true;
        }

        int radius = -1;

        if (args.length == 1) {
            try {
                radius = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                sender.sendMessage(ChatColor.RED + "Usage: /refreshmaps [radius]");
                return true;
            }
        }

        Player player = (Player) sender;
        int count = 0;
        ItemStack[] items = radius == -1
                ? player.getInventory().getContents()
                : getItemFramesInRadius(player, radius);
        for (ItemStack item : items) {
            if (item == null || item.getType() != Material.FILLED_MAP) continue;
            MapMeta meta = (MapMeta) item.getItemMeta();

            MapView view = meta.getMapView();
            MapRenderer renderer = Util.getRenderer(view);
            if (renderer == null) continue;
            view.getRenderers().forEach(view::removeRenderer);
            view.addRenderer(renderer);
            ++count;
        }
        if (count == 0) {
            sender.sendMessage(ChatColor.RED + "No maps found to be refreshed.");
            return true;
        }
        sender.sendMessage(ChatColor.GREEN + "Refreshed " + count + " map" + (count == 1 ? "" : "s") + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
