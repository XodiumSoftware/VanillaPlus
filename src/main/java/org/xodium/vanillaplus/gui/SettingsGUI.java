package org.xodium.vanillaplus.gui;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.xodium.vanillaplus.Database;
import org.xodium.vanillaplus.VanillaPlus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class SettingsGUI implements Listener {
    private static final Database DB = new Database();
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Component GUI_TITLE = MM.deserialize(VanillaPlus.MM_HEX_PREFIX + "Settings");
    private static Inventory INV;

    static {
        populateInventory();
    }

    private static void populateInventory() {
        Map<String, String> settings = DB.getAllData();
        int size = Math.min(54, Math.max(9, ((settings.size() - 1) / 9 + 1) * 9));
        INV = Bukkit.createInventory(null, size, GUI_TITLE);

        int slot = 0;
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            if (slot >= INV.getSize())
                break;
            INV.setItem(slot++, createItem(Material.PAPER, "<gold>" + entry.getKey(), "<gray>" + entry.getValue()));
        }
    }

    // TODO: use directly instead.
    @SuppressWarnings("unused")
    private static void updateSettingInDatabase(String name, String material) {
        DB.setData(name, material, false);
    }

    public static void openInventory(Player p) {
        p.openInventory(INV);
    }

    private static ItemStack createItem(Material m, String name, String lore) {
        ItemStack is = new ItemStack(m, 1);
        ItemMeta im = is.getItemMeta();
        im.displayName(MM.deserialize(name));
        im.lore(List.of(MM.deserialize(lore)));
        is.setItemMeta(im);
        return is;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory().equals(INV) && e.getCurrentItem() != null
                && e.getCurrentItem().getType() != Material.AIR) {
            e.setCancelled(true);
        }
    }

    // TODO: doesnt work.
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getInventory().equals(INV)) {
            e.setCancelled(true);
        }
    }
}
