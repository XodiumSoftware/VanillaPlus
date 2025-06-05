/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.mapify.command;

import com.funnyboyroks.mapify.util.Util;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.xodium.vanillaplus.mapify.Mapify;
import org.xodium.vanillaplus.mapify.PluginConfig;
import org.xodium.vanillaplus.mapify.PluginConfig.Diff;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class CommandMapify implements CommandExecutor, TabCompleter {

    private static final Map<UUID, Long> cooldownMap = new HashMap<>();

    public boolean reloadConfig(CommandSender sender) {
        PluginConfig oldConfig = Mapify.INSTANCE.config;
        try {
            Mapify.INSTANCE.reloadConfig();
            Mapify.INSTANCE.loadConfig();
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "There was an error reading the config: %s".formatted(e.getMessage()));
            e.printStackTrace();
            return true;
        }

        var diff = oldConfig.diff(Mapify.INSTANCE.config);

        if (diff.isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully with no changes.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully.");
            for (Diff d : diff) {
                sender.sendMessage(ChatColor.YELLOW + d.key + ": " + ChatColor.AQUA + d.old + ChatColor.YELLOW + " → " + ChatColor.AQUA + d.neu);
            }
        }

        return true;
    }

    public boolean commandConfig(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        }

        var field = args[0];

        switch (field) {
            case PluginConfig.Keys.WHITELIST_IS_BLACKLIST: {
                if (args[1].equalsIgnoreCase("true")) {
                    Mapify.INSTANCE.config.whitelistIsBlacklist = true;
                } else if (args[1].equalsIgnoreCase("false")) {
                    Mapify.INSTANCE.config.whitelistIsBlacklist = false;
                } else {
                    sender.sendMessage(ChatColor.RED + "%s may only be set to `true` or `false`".formatted(field));
                    return true;
                }
            }
            break;
            case PluginConfig.Keys.WHITELIST: {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /mapify config %s <add|remove> <domain>".formatted(field));
                    return true;
                }

                var url = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                var url2 = Util.getUrl(url);
                var host = url2 == null ? url : url2.getHost();
                if (args[1].equalsIgnoreCase("add")) {
                    var whitelist = Mapify.INSTANCE.config.whitelist;
                    if (!whitelist.contains(host)) {
                        whitelist.add(host);
                    }
                } else if (args[1].equalsIgnoreCase("remove")) {
                    Mapify.INSTANCE.config.whitelist.remove(host);
                } else {
                    sender.sendMessage(ChatColor.RED + "%s may only perform `add` or `remove`".formatted(field));
                    return true;
                }
            }
            break;
            case PluginConfig.Keys.CACHE_DURATION: {
                var n = Util.tryParseInt(args[1]);
                if (n == null) {
                    sender.sendMessage(ChatColor.RED + "%s may only be set to an integer.".formatted(field));
                    return true;
                } else {
                    Mapify.INSTANCE.config.cacheDuration = n;
                }
            }
            break;
            case PluginConfig.Keys.SAVE_IMAGES: {
                if (args[1].equalsIgnoreCase("true")) {
                    Mapify.INSTANCE.config.saveImages = true;
                } else if (args[1].equalsIgnoreCase("false")) {
                    Mapify.INSTANCE.config.saveImages = false;
                } else {
                    sender.sendMessage(ChatColor.RED + "%s may only be set to `true` or `false`".formatted(field));
                    return true;
                }
            }
            break;
            case PluginConfig.Keys.DEBUG_LOGGING: {
                if (args[1].equalsIgnoreCase("true")) {
                    Mapify.INSTANCE.config.debug = true;
                } else if (args[1].equalsIgnoreCase("false")) {
                    Mapify.INSTANCE.config.debug = false;
                } else {
                    sender.sendMessage(ChatColor.RED + "%s may only be set to `true` or `false`".formatted(field));
                    return true;
                }
            }
            break;
            case PluginConfig.Keys.COOLDOWN: {
                var n = Util.tryParseInt(args[1]);
                if (n == null) {
                    sender.sendMessage(ChatColor.RED + "%s may only be set to an integer.".formatted(field));
                    return true;
                } else {
                    Mapify.INSTANCE.config.cooldown = n;
                }
            }
            break;
            case PluginConfig.Keys.OP_COOLDOWN: {
                var n = Util.tryParseInt(args[1]);
                if (n == null) {
                    sender.sendMessage(ChatColor.RED + "%s may only be set to an integer.".formatted(field));
                    return true;
                } else {
                    Mapify.INSTANCE.config.opCooldown = n;
                }
            }
            break;
            case PluginConfig.Keys.MAX_SIZE: {
                Mapify.INSTANCE.config.maxSize = args[1];
            }
            break;
            default: {
                sender.sendMessage(ChatColor.RED + "Unknown field '%s'".formatted(field));
            }
            break;
        }
        Mapify.INSTANCE.config.save(Mapify.INSTANCE);
        sender.sendMessage(ChatColor.GREEN + "Config updated!");

        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mapify.command.mapify")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to run this command.");
            return true;
        }

        if (args.length == 1 && args[0].equals("reload")) {
            if (!sender.hasPermission("mapify.command.mapify.reload")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to run this command.");
                return true;
            }
            return reloadConfig(sender);
        }

        if (args.length >= 1 && args[0].equals("config")) {
            if (!sender.hasPermission("mapify.command.mapify.config")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to run this command.");
                return true;
            }
            ;
            return commandConfig(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used in-game.");
            return true;
        }

        Player player = (Player) sender;
        Long cdEnd = cooldownMap.get(player.getUniqueId());
        if (cdEnd != null) {
            long current = System.currentTimeMillis();
            long remaining = (cdEnd - current) / 1000;
            if (remaining > 0) {
                sender.sendMessage(ChatColor.RED + "You can use this command again in %d seconds.".formatted(remaining));
                return true;
            }
        }

        if (args.length == 0 || args.length > 2) {
            return false;
        }

        URL url = Util.getUrl(args[0]);

        if (url == null) {
            player.sendMessage(ChatColor.RED + "Please specify a valid URL.");
            return true;
        }

        if (!Util.isAllowed(url)) {
            if (Util.isOperator(player)) {
                player.sendMessage(ChatColor.RED + "This domain is not whitelisted in your Mapify config.");
                player.sendMessage(ChatColor.RED + "Run " + ChatColor.DARK_RED + "/mapify config whitelist add " + url.getHost() + ChatColor.RED + " to automatically add it to the config.");
            } else {
                player.sendMessage(ChatColor.RED + "This is not a valid domain, please contact your server administrators if you believe this is an issue.");
            }
            return true;
        }

        if (!url.getProtocol().equalsIgnoreCase("https") && Mapify.INSTANCE.config.httpsOnly) {
            player.sendMessage(ChatColor.RED + "This URL is not https. You must use a URL with https.");
            return true;
        }

        Point dims = new Point(1, 1);
        if (args.length == 2) {
            dims = Util.getDimensions(args[1]);
            if (dims == null) {
                player.sendMessage(ChatColor.RED + "Invalid dimensions, must be in the form WIDTHxHEIGHT: \"1x1\".");
                return true;
            }
        }

        if (!Util.dimsMatch(dims, Mapify.INSTANCE.config.maxSize)) {
            player.sendMessage(ChatColor.RED + "Dimensions speified are too large.");
            return true;
        }

        List<ItemStack> stacks = Util.getMaps(args[0], dims.x, dims.y);

        if (stacks == null) {
            player.sendMessage(ChatColor.RED + "This URL does not have an image.");
            return true;
        }

        int cooldown = sender.hasPermission("mapify.operator") ? Mapify.INSTANCE.config.opCooldown : Mapify.INSTANCE.config.cooldown;
        if (cooldown > 0) {
            long end = System.currentTimeMillis() + cooldown * 1000;
            cooldownMap.put(player.getUniqueId(), end);
            Bukkit.getScheduler().runTaskLater(Mapify.INSTANCE, () -> {
                cooldownMap.remove(player.getUniqueId());
            }, cooldown * 20L);
        }

        Util.giveItems(player, stacks.toArray(ItemStack[]::new));

        player.sendMessage(ChatColor.GREEN + "Given " + stacks.size() + " map" + (stacks.size() == 1 ? "." : "s."));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 2
                ? sender.hasPermission("mapify.comamnd.mapify.reload") && args[0].equals("reload")
                ? Collections.emptyList()
                : List.of("1x1")
                : sender.hasPermission("mapify.comamnd.mapify.reload")
                ? List.of("reload")
                : Collections.emptyList();
    }
}
