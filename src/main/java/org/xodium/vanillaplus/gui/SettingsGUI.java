package org.xodium.vanillaplus.gui;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.xodium.vanillaplus.Database;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * A simple GUI for plugin settings.
 */
public class SettingsGUI implements Listener {
    private static final Database DB = new Database();
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Component GUI_TITLE = MM.deserialize("Settings");
    private static final Inventory INV = Bukkit.createInventory(null, 9, GUI_TITLE);

    static {
        populateInventory();
    }

    /**
     * Loads settings from the SQLite database and populates the inventory.
     */
    private static void populateInventory() {
        Map<String, String> settings = DB.getAllSettings();
        int slot = 0;
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            if (slot >= INV.getSize())
                break;
            String name = entry.getKey();
            String material = entry.getValue();
            Material mat = Material.matchMaterial(material);
            if (mat != null) {
                INV.setItem(slot++, createItem(mat, MM.deserialize(name)));
            }
        }
    }

    /**
     * Updates a setting in the SQLite database.
     *
     * @param name     the setting name.
     * @param material the material associated with the setting.
     */
    private static void updateSettingInDatabase(String name, String material) {
        DB.setData(name, material, false);
    }

    /**
     * Opens the settings inventory for a player.
     *
     * @param p the player.
     */
    public static void openInventory(Player p) {
        p.openInventory(INV);
    }

    /**
     * Creates a GUI item.
     *
     * @param m    the material.
     * @param name the display name.
     * @return the ItemStack.
     */
    private static ItemStack createItem(Material m, Component name) {
        ItemStack is = new ItemStack(m, 1);
        ItemMeta im = is.getItemMeta();
        im.displayName(name);
        is.setItemMeta(im);
        return is;
    }

    /**
     * Handles clicks in the settings inventory.
     *
     * @param event the click event.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(INV))
            return;

        e.setCancelled(true);
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR)
            return;

        updateSettingInDatabase(MM.serialize(clickedItem.getItemMeta().displayName()), clickedItem.getType().name());
        // TODO: Provide feedback to the player, e.g., via chat or title.
    }
}
