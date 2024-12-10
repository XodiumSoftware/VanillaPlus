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

/**
 * A simple GUI for plugin settings.
 */
public class SettingsGUI implements Listener {

    private static final String GUI_TITLE = "Settings";
    private static final Component OPTION_1_NAME = Component.text("Option 1");
    private static final Component OPTION_2_NAME = Component.text("Option 2");

    private static Inventory inv;

    /**
     * Opens the settings inventory for a player.
     *
     * @param p the player.
     */
    public static void openInventory(Player p) {
        if (inv == null) {
            initInventory();
        }
        p.openInventory(inv);
    }

    /**
     * Initializes the inventory if it hasn't been created yet.
     */
    private static void initInventory() {
        inv = Bukkit.createInventory(null, 9, Component.text(GUI_TITLE));
        initItems();
    }

    /**
     * Populates the inventory with items.
     */
    private static void initItems() {
        inv.setItem(0, createItem(Material.COMPASS, OPTION_1_NAME));
        inv.setItem(1, createItem(Material.REDSTONE, OPTION_2_NAME));
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
     * @param e the click event.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!isSettingsInventory(e))
            return;

        e.setCancelled(true);

        ItemStack is = e.getCurrentItem();
        if (is == null || is.getType() == Material.AIR)
            return;

        handleClick(is.getItemMeta().displayName());
    }

    /**
     * Checks if the inventory is the settings inventory.
     *
     * @param e the click event.
     * @return true if it is; false otherwise.
     */
    private static boolean isSettingsInventory(InventoryClickEvent e) {
        return e.getInventory().equals(inv);
    }

    /**
     * Handles item click logic.
     *
     * @param name the clicked item's display name.
     */
    private static void handleClick(Component name) {
        if (OPTION_1_NAME.equals(name)) {
            // TODO: Implement Option 1 logic.
        } else if (OPTION_2_NAME.equals(name)) {
            // TODO: Implement Option 2 logic.
        }
    }
}
