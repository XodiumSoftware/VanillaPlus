package org.xodium.vanillaplus;

import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.nio.file.Paths;

public class ConfigManager {
    private static final String CONFIG_FILE_PATH = "config.yml";
    private final VanillaPlus vp = VanillaPlus.getInstance();
    final ConfigurationLoader<BasicConfigurationNode> cl;
    BasicConfigurationNode bcn;

    {
        cl = GsonConfigurationLoader.builder()
                .path(Paths.get(CONFIG_FILE_PATH)).build();
        try {
            bcn = cl.load();
        } catch (ConfigurateException e) {
            e.printStackTrace();
            vp.getServer().getPluginManager().disablePlugin(vp);
        }
    }

    public void saveConfig() {
        try {
            cl.save(bcn);
        } catch (ConfigurateException e) {
            e.printStackTrace();
            vp.getServer().getPluginManager().disablePlugin(vp);
        }
    }

    public void setConfigValue(String key, Object value) {
        try {
            bcn.node(key).set(value);
        } catch (SerializationException e) {
            e.printStackTrace();
        }
        saveConfig();
    }

    public <T> T getConfigValue(String key, Class<T> clazz) {
        try {
            return bcn.node(key).get(clazz);
        } catch (SerializationException e) {
            e.printStackTrace();
        }
        return null;
    }
}