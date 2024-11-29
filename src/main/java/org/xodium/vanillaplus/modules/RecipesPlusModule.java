package org.xodium.vanillaplus.modules;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Recipe;

public class RecipesPlusModule implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Iterator<Recipe> iter = Bukkit.recipeIterator();
        while (iter.hasNext()) {
            Recipe r = iter.next();
            if (r instanceof Keyed) {
                NamespacedKey k = ((Keyed) r).getKey();
                if (!p.hasDiscoveredRecipe(k)) {
                    p.discoverRecipe(k);
                }
            }
        }
    }
}
