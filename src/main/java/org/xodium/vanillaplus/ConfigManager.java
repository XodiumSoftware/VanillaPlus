package org.xodium.vanillaplus;

import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
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
            cl.save(bcn);
        } catch (ConfigurateException e) {
            e.printStackTrace();
            vp.getServer().getPluginManager().disablePlugin(vp);
        }
    }
}