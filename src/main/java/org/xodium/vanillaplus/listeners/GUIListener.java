package org.xodium.vanillaplus.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.xodium.vanillaplus.data.GUISettings;
import org.xodium.vanillaplus.interfaces.CONFIG;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

// TODO: add action to each setting (based on value) and save the value to the database.
// TODO: fix 12 items but only 9 are shown in the GUI, second row is empty while it should contain those 3 other items.
public class GUIListener implements Listener {
    private final static Database db = new Database();
    private static final Map<Player, Inventory> playerInventories = new HashMap<>();
    private static final Map<Player, Integer> playerPageIndices = new HashMap<>();
    private static final List<GUISettings> settings = new ArrayList<>();
    static {
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

    public static void openGUI(Player player) {
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

        for (int i = 0; i < inventorySize - 9; i++) {
            int settingIndex = (currentPageIndex * maxSlotsPerInventory) + i;
            if (settingIndex >= settingsCount)
                break;

            GUISettings setting = settings.get(settingIndex);
            ItemStack item = new ItemStack(setting.getMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.displayName(mm.deserialize(setting.getDisplayName()));

            List<Component> loreComponents = new ArrayList<>();
            for (String key : setting.getLore()) {
                Object value = db.getData(key);
                loreComponents.add(Component.text(value != null ? value.toString() : "N/A"));
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        MiniMessage mm = MiniMessage.miniMessage();
        if (mm.serialize(e.getView().title()).contains("VanillaPlus Settings")) {
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            ItemStack clickedItem = e.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR)
                return;

            if (clickedItem.getType() == Material.ARROW) {
                String clickedItemName = mm.serialize(clickedItem.getItemMeta().displayName());

                if (clickedItemName.contains("Previous Page")) {
                    if (playerPageIndices.getOrDefault(player, 0) > 0) {
                        playerPageIndices.put(player, playerPageIndices.get(player) - 1);
                        openGUI(player);
                    }
                    return;
                } else if (clickedItemName.contains("Next Page")) {
                    int settingsCount = settings.size();
                    int maxSlotsPerInventory = 54;
                    if (playerPageIndices.getOrDefault(player,
                            0) < (int) Math.ceil(settingsCount / (double) maxSlotsPerInventory) - 1) {
                        playerPageIndices.put(player, playerPageIndices.get(player) + 1);
                        openGUI(player);
                    }
                    return;
                }
            }

            settings.stream()
                    .filter(setting -> setting.getMaterial() == clickedItem.getType())
                    .findFirst()
                    .ifPresent(setting -> setting.getAction().accept(player));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        playerInventories.remove(e.getPlayer());
    }
}
