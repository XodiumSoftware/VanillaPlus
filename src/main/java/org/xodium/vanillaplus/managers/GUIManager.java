package org.xodium.vanillaplus.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.xodium.vanillaplus.Database;
import org.xodium.vanillaplus.data.GUISettings;
import org.xodium.vanillaplus.interfaces.CONFIG;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

// TODO: add action to each setting (based on value) and save the value to the database.
// TODO: fix 12 items but only 9 are shown in the GUI, second row is empty while it should contain those 3 other items.
public class GUIManager {
    private final static Database db = new Database();
    public final Map<Player, Inventory> playerInventories = new HashMap<>();
    public final Map<Player, Integer> playerPageIndices = new HashMap<>();
    public final List<GUISettings> settings = new ArrayList<>();
    {
        settings.add(
                new GUISettings(Material.OAK_DOOR, "Knock on Wooden Door", List.of(CONFIG.SOUND_KNOCK_WOOD), (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(new GUISettings(Material.BELL, "Set Knock Volume", List.of(CONFIG.SOUND_KNOCK_VOLUME), (p) -> {
            // This is where the action should be performed
        }));
        settings.add(new GUISettings(Material.NOTE_BLOCK, "Set Knock Pitch", List.of(CONFIG.SOUND_KNOCK_PITCH), (p) -> {
            // This is where the action should be performed
        }));
        settings.add(
                new GUISettings(Material.JUKEBOX, "Set Knock Category", List.of(CONFIG.SOUND_KNOCK_CATEGORY), (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(new GUISettings(Material.LEVER, "Allow Knocking", List.of(CONFIG.ALLOW_KNOCKING), (p) -> {
            // This is where the action should be performed
        }));
        settings.add(new GUISettings(Material.SPRUCE_TRAPDOOR, "Allow Knocking on Trapdoors",
                List.of(CONFIG.ALLOW_KNOCKING_TRAPDOORS), (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(new GUISettings(Material.OAK_FENCE_GATE, "Allow Knocking on Gates",
                List.of(CONFIG.ALLOW_KNOCKING_GATES), (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(new GUISettings(Material.CLOCK, "Allow Auto-Close", List.of(CONFIG.ALLOW_AUTOCLOSE), (p) -> {
            // This is where the action should be performed
        }));
        settings.add(
                new GUISettings(Material.RED_CARPET, "Require Shift to Knock", List.of(CONFIG.KNOCKING_REQUIRES_SHIFT),
                        (p) -> {
                            // This is where the action should be performed
                        }));
        settings.add(new GUISettings(Material.BARRIER, "Require Empty Hand to Knock",
                List.of(CONFIG.KNOCKING_REQUIRES_EMPTY_HAND), (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(
                new GUISettings(Material.SPRUCE_DOOR, "Allow Double Doors", List.of(CONFIG.ALLOW_DOUBLEDOORS), (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(new GUISettings(Material.CLOCK, "Set Auto-Close Delay", List.of(CONFIG.AUTOCLOSE_DELAY), (p) -> {
            // This is where the action should be performed
        }));
    }

    public void openGUI(Player player) {
        int settingsCount = settings.size();
        int maxSlotsPerInventory = 54;
        int inventoryCount = (int) Math.ceil(settingsCount / (double) maxSlotsPerInventory);

        if (playerInventories.containsKey(player)) {
            player.closeInventory();
        }

        int currentPageIndex = playerPageIndices.getOrDefault(player, 0);
        int inventorySize = Math.min(maxSlotsPerInventory, settingsCount - (currentPageIndex * maxSlotsPerInventory));
        inventorySize = (int) Math.ceil(inventorySize / 9.0) * 9;
        MiniMessage mm = MiniMessage.miniMessage();
        Inventory gui = Bukkit.createInventory(null, inventorySize,
                mm.deserialize("<b><gradient:#CB2D3E:#EF473A>VanillaPlus Settings</gradient></b>"));

        int startIdx = currentPageIndex * maxSlotsPerInventory;
        for (int i = 0; i < inventorySize - 9 && startIdx + i < settingsCount; i++) {
            GUISettings setting = settings.get(startIdx + i);
            ItemStack item = new ItemStack(setting.getMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.displayName(mm.deserialize(setting.getDisplayName()));

            List<Component> loreComponents = new ArrayList<>();
            for (String key : setting.getLore()) {
                Object value = db.getData(key);
                loreComponents.add(mm.deserialize(value != null ? value.toString() : "N/A"));
            }

            meta.lore(loreComponents);
            item.setItemMeta(meta);
            gui.setItem(i, item);
        }

        if (inventoryCount > 1) {
            if (currentPageIndex > 0) {
                ItemStack previousButton = new ItemStack(Material.ARROW);
                ItemMeta previousMeta = previousButton.getItemMeta();
                previousMeta.displayName(mm.deserialize("Previous Page"));
                previousButton.setItemMeta(previousMeta);
                gui.setItem(gui.getSize() - 9, previousButton);
            }

            if (currentPageIndex < inventoryCount - 1) {
                ItemStack nextButton = new ItemStack(Material.ARROW);
                ItemMeta nextMeta = nextButton.getItemMeta();
                nextMeta.displayName(mm.deserialize("Next Page"));
                nextButton.setItemMeta(nextMeta);
                gui.setItem(gui.getSize() - 1, nextButton);
            }
        }

        playerInventories.put(player, gui);
        player.openInventory(gui);
    }
}
