package org.xodium.vanillaplus;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.File;

public class ConfigManager {
    ConfigurationLoader<ConfigurationNode> cl = GsonConfigurationLoader.builder().file(new File("config.yml")).build();
    ConfigurationNode cn = cl.load();
    cn.node("").set("");
    cl.save(cn);
}
