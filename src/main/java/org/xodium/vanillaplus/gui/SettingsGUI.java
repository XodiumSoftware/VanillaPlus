package org.xodium.vanillaplus.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;

public class SettingsGUI implements Listener {

    private static Inventory inv;

    public SettingsGUI() {
        inv = Bukkit.createInventory(null, 9, Component.text("Settings"));
        initializeItems();
    }

    private void initializeItems() {
        inv.setItem(0, createGuiItem(Material.COMPASS, "Option 1"));
        inv.setItem(1, createGuiItem(Material.REDSTONE, "Option 2"));
        // Add more items as needed
    }

    private ItemStack createGuiItem(Material m, String name) {
        ItemStack is = new ItemStack(m, 1);
        ItemMeta im = is.getItemMeta();
        im.displayName(Component.text(name));
        is.setItemMeta(im);
        return is;
    }

    public static void openInventory(Player p) {
        p.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inv))
            return;
        e.setCancelled(true);
        ItemStack is = e.getCurrentItem();
        if (is == null || is.getType() == Material.AIR)
            return;

        Component name = is.getItemMeta().displayName();
        if (name.equals("Option 1")) {
            // Handle Option 1
        } else if (name.equals("Option 2")) {
            // Handle Option 2
        }
    }
}