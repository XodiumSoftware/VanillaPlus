package org.xodium.vanillaplus.modules;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Recipe;

public class RecipesPlusModule implements Listener {
    // TODO: doesnt work.
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        for (Recipe recipe : Bukkit.getRecipesFor(null)) {
            if (recipe instanceof Keyed) {
                NamespacedKey key = ((Keyed) recipe).getKey();
                if (!player.hasDiscoveredRecipe(key)) {
                    player.discoverRecipe(key);
                }
            }
        }
    }
}