package org.xodium.vanillaplus.modules;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Recipe;
import org.xodium.vanillaplus.interfaces.Modular;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RecipesModule implements Listener, Modular {
    public static final String ENABLE = ".enable";
    private final String className = RecipesModule.class.getSimpleName();

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
    public Map<String, Object> config() {
        return new HashMap<String, Object>() {
            {
                put(className + ENABLE, true);
            }
        };
    }
}