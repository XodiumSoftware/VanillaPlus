package org.xodium.vanillaplus.modules;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Recipe;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.interfaces.Modular;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RecipesModule implements Listener, Modular {
    private final VanillaPlus vp = VanillaPlus.getInstance();
    private final FileConfiguration fc = vp.getConfig();
    private final String className = getClass().getSimpleName();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Iterator<Recipe> rIter = Bukkit.recipeIterator();
        while (rIter.hasNext()) {
            Recipe r = rIter.next();
            if (r instanceof Keyed) {
                NamespacedKey k = ((Keyed) r).getKey();
                if (!p.hasDiscoveredRecipe(k)) {
                    p.discoverRecipe(k);
                }
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return fc.getBoolean(className + ENABLE);
    }

    @Override
    public Map<String, Object> config() {
        Map<String, Object> fcMap = new HashMap<>();
        fcMap.put(className + ENABLE, true);
        fc.addDefaults(fcMap);
        vp.saveConfig();
        return fcMap;
    }
}