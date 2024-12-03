package org.xodium.vanillaplus;

import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.xodium.vanillaplus.interfaces.CONFIG;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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

            Map<String, Object> doorsModuleMap = createDoorsModuleMap();

            populateConfigNode(doorsModuleMap, CONFIG.DOORSMODULE.PREFIX);

            cl.save(bcn);
        } catch (ConfigurateException e) {
            e.printStackTrace();
            vp.getServer().getPluginManager().disablePlugin(vp);
        }
    }

    private Map<String, Object> createDoorsModuleMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(CONFIG.DOORSMODULE.ENABLE, true);
        map.put(CONFIG.DOORSMODULE.SOUND_KNOCK_CATEGORY, "BLOCKS");
        map.put(CONFIG.DOORSMODULE.SOUND_KNOCK_PITCH, 1.0);
        map.put(CONFIG.DOORSMODULE.SOUND_KNOCK_VOLUME, 1.0);
        map.put(CONFIG.DOORSMODULE.SOUND_KNOCK_WOOD, "entity_zombie_attack_wooden_door");
        map.put(CONFIG.DOORSMODULE.ALLOW_AUTOCLOSE, true);
        map.put(CONFIG.DOORSMODULE.ALLOW_DOUBLEDOORS, true);
        map.put(CONFIG.DOORSMODULE.ALLOW_KNOCKING, true);
        map.put(CONFIG.DOORSMODULE.ALLOW_KNOCKING_GATES, true);
        map.put(CONFIG.DOORSMODULE.ALLOW_KNOCKING_TRAPDOORS, true);
        map.put(CONFIG.DOORSMODULE.KNOCKING_REQUIRES_EMPTY_HAND, true);
        map.put(CONFIG.DOORSMODULE.KNOCKING_REQUIRES_SHIFT, false);
        map.put(CONFIG.DOORSMODULE.AUTOCLOSE_DELAY, 6);
        return map;
    }

    private void populateConfigNode(Map<String, Object> configMap, String prefix) {
        try {
            for (Map.Entry<String, Object> entry : configMap.entrySet()) {
                bcn.node(CONFIG.SETTINGS, prefix, entry.getKey()).set(entry.getValue());
            }
        } catch (SerializationException e) {
            e.printStackTrace();
            vp.getServer().getPluginManager().disablePlugin(vp);
        }
    }
}