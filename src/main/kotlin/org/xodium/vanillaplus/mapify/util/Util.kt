/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.mapify.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.xodium.vanillaplus.mapify.Mapify;
import org.xodium.vanillaplus.mapify.PluginData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Util {

    public static CompletableFuture<Boolean> isLatestVersion() {

        int serverVersion = Integer.parseInt(
                Mapify.INSTANCE
                        .getDescription()
                        .getVersion()
                        .replaceAll("\\.|-SNAPSHOT|v", "")
        );

        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL("https://api.modrinth.com/v2/project/wsr1TOgJ");
                InputStreamReader reader = new InputStreamReader(url.openStream());
                JsonArray versions = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("versions");
                String version = versions.get(versions.size() - 1).getAsString();

                url = new URL("https://api.modrinth.com/v2/version/" + version);
                reader = new InputStreamReader(url.openStream());
                int latestVersion = Integer.parseInt(
                        JsonParser.parseReader(reader)
                                .getAsJsonObject()
                                .get("version_number")
                                .getAsString()
                                .replaceAll("\\.|-SNAPSHOT|v", "")
                );
                Mapify.INSTANCE.getLogger().info("Latest Version: " + latestVersion);

                return latestVersion <= serverVersion;
            } catch (IOException e) {
                Mapify.INSTANCE.getLogger().severe("Unable to contact Modrinth API to check version!");
                return true;
            }
        });


    }

    public static URL getUrl(String arg) {
        try {
            return new URL(arg);
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    public static Image getImage(URL url) {
        if (Mapify.INSTANCE.config.debug) {
            Mapify.INSTANCE.getLogger().info("Fetching image from " + url);
        }
        File imgFile = Util.getImageFile(url);
        if (Mapify.INSTANCE.config.saveImages) {
            if (imgFile.exists()) {
                try {
                    return ImageIO.read(imgFile);
                } catch (IOException e) {
                    Mapify.INSTANCE.getLogger().severe(String.format("Invalid image on disk: %s\n\tThis file should be removed by the server owner.  Downloading image from url...", imgFile.getPath()));
                    // This does not return so it downloads the image
                }
            } else {
                if (Mapify.INSTANCE.config.debug) {
                    Mapify.INSTANCE.getLogger().info("File not found on system.  Downloading...");
                }
            }
        }
        try {
            Image image = ImageIO.read(url);
            if (Mapify.INSTANCE.config.saveImages) {
                ImageIO.write((RenderedImage) image, "png", imgFile);
            }
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            Mapify.INSTANCE.getLogger().severe("Invalid image url: " + url);
            return null;
        }
    }

    public static File getImageFile(URL url) {
        // SHA-256 is supposed to be collision proof (I think) and the actual result doesn't matter, as long as it's consistent
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            // This should only be called if the config is enabled, so it's okay to throw
            // Possible TODO: Convert this to use a different method?
            //     The concern with ^ is that is loses its consistency guarantee
            throw new RuntimeException(ex);
        }
        byte[] bytes = md.digest(url.toString().getBytes(StandardCharsets.UTF_8));
        String name = Util.bytesToString(bytes);

        // It should always be .png
        name += ".png";

        return Paths.get(Mapify.INSTANCE.getDataFolder().getPath(), "img", name).toFile();
    }

    public static ItemStack createMap(String url, int x, int y, int w, int h) {
        ItemStack stack = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) stack.getItemMeta();
        assert meta != null;

        MapView view = Bukkit.getServer().createMap(Bukkit.getServer().getWorlds().get(0));
        Mapify.INSTANCE.dataHandler.data.mapData.put(view.getId(), new PluginData.MapData(url, x, y, w, h));
        Mapify.INSTANCE.dataHandler.dirty();


        view.getRenderers().forEach(view::removeRenderer);
        MapRenderer renderer = Util.getRenderer(view);
        if (renderer == null) return null;
        view.addRenderer(renderer);

        meta.setMapView(view);


        List<String> lore = meta.getLore();
        lore = lore == null ? new ArrayList<>() : lore;
        lore.add(0, "Position: (" + x + ", " + y + ")");
        meta.setLore(lore);

        stack.setItemMeta(meta);
        return stack;
    }

    public static List<ItemStack> getMaps(String url, int width, int height) {
        List<ItemStack> out = new ArrayList<>();

        var u = getUrl(url);
        if (u == null) return null;
        var img = Mapify.INSTANCE.imageCache.get(u);
        if (img == null) return null;

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                out.add(createMap(url, x, y, width, height));
            }
        }

        return out;
    }

    public static Integer tryParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Point getDimensions(String str) {
        try {
            String[] parts = str.split("x");
            int w = Integer.parseInt(parts[0]);
            int h = Integer.parseInt(parts[1]);
            return new Point(w, h);
        } catch (Exception ex) {
            return null;
        }
    }

    public static void giveItems(Player player, ItemStack... stacks) {
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(stacks);

        if (overflow.isEmpty()) return;

        player.sendMessage(
                ChatColor.RED +
                        String.format(
                                "You were given %d item%s, but only %d fit. The others have been dropped on the ground.",
                                stacks.length,
                                stacks.length == 1 ? "" : "s",
                                stacks.length - overflow.size()
                        ));
        overflow.forEach((k, stack) -> player.getWorld().dropItem(player.getLocation(), stack));
    }

    public static MapRenderer getRenderer(MapView view) {

        PluginData.MapData data = Mapify.INSTANCE.dataHandler.data.mapData.get(view.getId());
        Mapify.INSTANCE.dataHandler.dirty();

        if (data == null) return null;

        Image img = Mapify.INSTANCE.imageCache.get(getUrl(data.url));
        if (img == null) return null;
        return new CustomMapRenderer(img, data.x, data.y, data.scaleX, data.scaleY);
    }

    public static boolean isOperator(CommandSender player) {
        return player.hasPermission("mapify.operator");
    }

    public static boolean isAllowed(URL url) {
        String host = url.getHost();

        List<String> whitelist = Mapify.INSTANCE.config.whitelist;

        for (String s : whitelist) {
            if (s.toLowerCase().startsWith("regexp:")) {
                if (s.matches(s.substring("regexp:".length()))) {
                    return !Mapify.INSTANCE.config.whitelistIsBlacklist;
                }
            } else {
                if (s.equalsIgnoreCase(host)) return !Mapify.INSTANCE.config.whitelistIsBlacklist;
            }
        }
        return Mapify.INSTANCE.config.whitelistIsBlacklist;
    }

    public static String bytesToString(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            if (b <= 0x0f) {
                out.append("0");
            }
            out.append(Integer.toHexString(0xff & b));
        }
        return out.toString();
    }

    public static boolean dimsMatch(Point dims, String max) {
        // "" means no filter
        if (max.isBlank()) return true;

        // WxH
        if (max.contains("x")) {
            // dims
            Point bounds = Util.getDimensions(max);
            if (bounds == null) return false; // invalid dims
            return dims.x <= bounds.x && dims.y <= bounds.y;
        }

        // N
        int area;
        try {
            area = Integer.parseInt(max);
        } catch (NumberFormatException ignored) {
            return false; // invalid number
        }

        return area <= 0 || dims.x * dims.y <= area;
    }
}
