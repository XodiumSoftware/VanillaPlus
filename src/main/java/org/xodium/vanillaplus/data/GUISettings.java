package org.xodium.vanillaplus.data;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GUISettings {
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final Consumer<Player> action;

    public GUISettings(Material material, String displayName, List<String> lore, Consumer<Player> action) {
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.action = action;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public Consumer<Player> getAction() {
        return action;
    }
}
