package org.xodium.vanillaplus.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.xodium.vanillaplus.Database;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class GUIListener implements Listener {
    private final Database db = new Database();

    public static void openGUI(Player p) {
        {
            Inventory gui = Bukkit.createInventory(null, 27, MiniMessage.miniMessage()
                    .deserialize("VanillaPlus Settings"));

            ItemStack toggleRedstone = new ItemStack(Material.REDSTONE_TORCH);
            ItemMeta redstoneMeta = toggleRedstone.getItemMeta();
            redstoneMeta.displayName(Component.text("Toggle Redstone"));
            toggleRedstone.setItemMeta(redstoneMeta);
            gui.setItem(11, toggleRedstone);

            ItemStack autoCloseDelay = new ItemStack(Material.CLOCK);
            ItemMeta delayMeta = autoCloseDelay.getItemMeta();
            delayMeta.displayName(Component.text("Set Auto-Close Delay"));
            autoCloseDelay.setItemMeta(delayMeta);
            gui.setItem(15, autoCloseDelay);

            p.openInventory(gui);

        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("Plugin Settings")) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            ItemStack clickedItem = e.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR)
                return;

            if (clickedItem.getType() == Material.REDSTONE_TORCH) {
                // Toggle redstone setting
                // boolean redstoneEnabled =
                // !Boolean.parseBoolean(db.getData("allow_redstone"));
                // db.updateData("allow_redstone", Boolean.toString(redstoneEnabled));
                // p.sendMessage(Component.text("Redstone enabled: " + redstoneEnabled));
            } else if (clickedItem.getType() == Material.CLOCK) {
                // Set auto-close delay - placeholder for actual implementation
                db.updateData("auto_close_delay", "60"); // Placeholder value
                p.sendMessage(Component.text("Auto-close delay set to 60 seconds."));
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getView().getTitle().equals("Plugin Settings")) {
            // Handle GUI close events if needed
        }
    }
}
