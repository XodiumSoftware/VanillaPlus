/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.mapify;

import com.funnyboyroks.mapify.command.CommandManager;
import com.funnyboyroks.mapify.util.Cache;
import com.funnyboyroks.mapify.util.Util;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public final class Mapify extends JavaPlugin {

    public static Mapify INSTANCE;

    public Cache<URL, Image> imageCache;
    public PluginConfig config;
    public CommandManager commandManager;
    public DataHandler dataHandler;

    public Mapify() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        try {
            Util.isLatestVersion().thenAccept((latest) -> {
                if (!latest) {
                    this.getLogger().warning("Mapify has an update!");
                    this.getLogger().warning("Get it from https://modrinth.com/plugin/mapify");
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        Metrics metrics = new Metrics(this, 17096);

        metrics.addCustomChart(new SingleLineChart("maps", () -> Mapify.INSTANCE.dataHandler.data.mapData.size()));
        metrics.addCustomChart(new SimplePie("blacklist", () -> Mapify.INSTANCE.config.whitelistIsBlacklist + ""));
        metrics.addCustomChart(new AdvancedPie("whitelist", () -> Mapify.INSTANCE.config.whitelist.stream().collect(Collectors.toMap(k -> k, v -> 1))));
        this.getLogger().info("Metrics loaded.");

        this.dataHandler = new DataHandler();
        int maps = dataHandler.data.mapData.size();
        this.getLogger().info("Loaded " + maps + " map" + (maps == 1 ? "" : "s") + ".");
        this.commandManager = new CommandManager(this);
        Bukkit.getPluginManager().registerEvents(new Listeners(), this);

        // Periodically save the data if changed (every 5 minutes)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
                this,
                () -> this.dataHandler.trySaveData(false),
                5 * 60 * 20L,
                5 * 60 * 20L
        ); // 5 minutes * 60 seconds * 20 ticks

        // Periodically force save the data (hourly)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
                this,
                () -> this.dataHandler.trySaveData(true),
                (60 + 3) * 60 * 20L, // wait 63 minutes so the two tasks never run at the same time
                60 * 60 * 20L
        ); // 60 minutes * 60 seconds * 20 ticks


        try {
            this.loadConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Clear the expired items every hour
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
                this,
                this.imageCache::clearExpired,
                0,
                60 * 60 * 20L
        ); // 60 minutes * 60 seconds * 20 ticks

    }

    public void loadConfig() throws IOException {
        this.reloadConfig();
        config = new PluginConfig(INSTANCE);

        if (this.config.saveImages) {
            File imgDir = Path.of(this.getDataFolder().getPath(), "img").toFile();
            if (!imgDir.exists()) {
                boolean mkdir = imgDir.mkdirs();
                if (!mkdir) {
                    this.getLogger().severe("Unable to create img directory.");
                }
            }
        }

        if (this.imageCache == null) {
            this.imageCache = new Cache<>((long) this.config.cacheDuration * 60 * 1000, Util::getImage);
        } else {
            this.imageCache.setCacheDuration(this.config.cacheDuration * 60 * 1000);
        }
    }

    @Override
    public void onDisable() {
        try {
            this.dataHandler.saveData(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
