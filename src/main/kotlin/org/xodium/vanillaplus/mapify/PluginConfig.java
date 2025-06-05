/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.mapify;

import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PluginConfig {

    private final File configFile;

    public boolean whitelistIsBlacklist;
    public List<String> whitelist;
    public int cacheDuration;
    public boolean httpsOnly;
    public boolean saveImages;
    public boolean debug;
    public int cooldown;
    public int opCooldown;
    public String maxSize;

    public PluginConfig(Mapify plugin) throws IOException {
        plugin.saveDefaultConfig();
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.update();

        FileConfiguration config = plugin.getConfig();

        this.whitelistIsBlacklist = config.getBoolean(Keys.WHITELIST_IS_BLACKLIST, true);
        this.whitelist = config.getStringList(Keys.WHITELIST);
        this.cacheDuration = config.getInt(Keys.CACHE_DURATION, 60);
        this.httpsOnly = config.getBoolean(Keys.HTTPS_ONLY, true);
        this.saveImages = config.getBoolean(Keys.SAVE_IMAGES, false);
        this.debug = config.getBoolean(Keys.DEBUG_LOGGING, false);
        this.cooldown = config.getInt(Keys.COOLDOWN, 0);
        this.opCooldown = config.getInt(Keys.OP_COOLDOWN, 0);
        this.maxSize = config.getString(Keys.MAX_SIZE, "");
    }

    public void update() throws IOException {
        ConfigUpdater.update(Mapify.INSTANCE, "config.yml", this.configFile);
    }

    public void save(Mapify plugin) {
        var config = plugin.getConfig();
        config.set(Keys.WHITELIST_IS_BLACKLIST, this.whitelistIsBlacklist);
        config.set(Keys.WHITELIST, this.whitelist);
        config.set(Keys.CACHE_DURATION, this.cacheDuration);
        config.set(Keys.HTTPS_ONLY, this.httpsOnly);
        config.set(Keys.SAVE_IMAGES, this.saveImages);
        config.set(Keys.DEBUG_LOGGING, this.debug);
        config.set(Keys.COOLDOWN, this.cooldown);
        config.set(Keys.OP_COOLDOWN, this.opCooldown);
        config.set(Keys.MAX_SIZE, this.maxSize);
        plugin.saveConfig();
    }

    public List<Diff> diff(PluginConfig neu) {
        List<Diff> out = new ArrayList<>();
        if (this.whitelistIsBlacklist != neu.whitelistIsBlacklist)
            out.add(new Diff(Keys.WHITELIST_IS_BLACKLIST, this.whitelistIsBlacklist, neu.whitelistIsBlacklist));
        if (!this.whitelist.equals(neu.whitelist))
            out.add(new Diff(Keys.WHITELIST, this.whitelist, neu.whitelist));
        if (this.cacheDuration != neu.cacheDuration)
            out.add(new Diff(Keys.CACHE_DURATION, this.cacheDuration, neu.cacheDuration));
        if (this.httpsOnly != neu.httpsOnly)
            out.add(new Diff(Keys.HTTPS_ONLY, this.httpsOnly, neu.httpsOnly));
        if (this.saveImages != neu.saveImages)
            out.add(new Diff(Keys.SAVE_IMAGES, this.saveImages, neu.saveImages));
        if (this.debug != neu.debug)
            out.add(new Diff(Keys.DEBUG_LOGGING, this.debug, neu.debug));
        if (this.cooldown != neu.cooldown)
            out.add(new Diff(Keys.COOLDOWN, this.cooldown, neu.cooldown));
        if (this.opCooldown != neu.opCooldown)
            out.add(new Diff(Keys.OP_COOLDOWN, this.opCooldown, neu.opCooldown));
        if (!this.maxSize.equals(neu.maxSize))
            out.add(new Diff(Keys.MAX_SIZE, this.maxSize, neu.maxSize));
        return out;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PluginConfig { ");
        sb.append("whitelistIsBlacklist: ");
        sb.append(this.whitelistIsBlacklist);
        sb.append(", ");
        sb.append("whitelist: ");
        sb.append(this.whitelist);
        sb.append(", ");
        sb.append("cacheDuration: ");
        sb.append(this.cacheDuration);
        sb.append(", ");
        sb.append("httpsOnly: ");
        sb.append(this.httpsOnly);
        sb.append(", ");
        sb.append("saveImages: ");
        sb.append(this.saveImages);
        sb.append(", ");
        sb.append("debug: ");
        sb.append(this.debug);
        sb.append(", ");
        sb.append("cooldown: ");
        sb.append(this.cooldown);
        sb.append(", ");
        sb.append("opCooldown: ");
        sb.append(this.opCooldown);
        sb.append(", ");
        sb.append("maxSize: ");
        sb.append(this.maxSize);
        sb.append(", ");
        sb.append("}");
        return sb.toString();
    }

    ;

    public static class Keys {
        public static final String WHITELIST_IS_BLACKLIST = "whitelist-is-blacklist";
        public static final String WHITELIST = "whitelist";
        public static final String CACHE_DURATION = "cache-duration";
        public static final String HTTPS_ONLY = "https-only";
        public static final String SAVE_IMAGES = "save-images";
        public static final String DEBUG_LOGGING = "debug-logging";
        public static final String COOLDOWN = "cooldown";
        public static final String OP_COOLDOWN = "op-cooldown";
        public static final String MAX_SIZE = "max-size";
    }

    // should be a record, but we're using old java
    public static class Diff {
        public String key;
        public String old;
        public String neu;

        public <T> Diff(String key, T old, T neu) {
            this.key = key;
            this.old = old == null ? null : old.toString();
            this.neu = neu == null ? null : neu.toString();
        }
    }

}
