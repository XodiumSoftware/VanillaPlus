package org.xodium.vanillaplus.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.xodium.vanillaplus.managers.GUIManager;

import net.kyori.adventure.text.minimessage.MiniMessage;

// TODO: fix user being able to move the item in the GUI.
public class GUIListener implements Listener {
    private static final int MAX_SLOTS_PER_INV = 54;
    private final GUIManager gm = new GUIManager();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        MiniMessage mm = MiniMessage.miniMessage();
        if (mm.serialize(e.getView().title()).contains("VanillaPlus Settings")) {
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR)
                return;

            if (clickedItem != null && clickedItem.getType() == Material.ARROW) {
                String clickedItemName = mm.serialize(clickedItem.getItemMeta().displayName());
                if (clickedItemName.contains("Previous Page")) {
                    if (gm.playerPageIndices.getOrDefault(player, 0) > 0) {
                        gm.playerPageIndices.put(player, gm.playerPageIndices.get(player) - 1);
                        gm.openGUI(player);
                    }
                } else if (clickedItemName.contains("Next Page")) {
                    if (gm.playerPageIndices.getOrDefault(player,
                            0) < (int) Math.ceil(gm.settings.size() / (double) MAX_SLOTS_PER_INV) - 1) {
                        gm.playerPageIndices.put(player, gm.playerPageIndices.get(player) + 1);
                        gm.openGUI(player);
                    }
                }
            }

            gm.settings.stream()
                    .filter(setting -> setting.getMaterial() == clickedItem.getType())
                    .findFirst()
                    .ifPresent(setting -> setting.getAction().accept(player));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        gm.playerInventories.remove(e.getPlayer());
    }
}
