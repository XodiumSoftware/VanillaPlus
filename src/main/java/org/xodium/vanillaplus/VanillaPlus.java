package org.xodium.vanillaplus;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.xodium.vanillaplus.commands.GUICommand;
import org.xodium.vanillaplus.interfaces.CONFIG;
import org.xodium.vanillaplus.listeners.DoorListener;

public class VanillaPlus extends JavaPlugin {

  private static VanillaPlus instance;
  private static final String V = "1.21.1";

  public static VanillaPlus getInstance() {
    return instance;
  }

  @Override
  public void onEnable() {
    if (!getServer().getVersion().contains(V)) {
      getLogger().severe("This plugin requires paper version: " + V);
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    instance = this;
    initDB();
    GUICommand.init(this.getLifecycleManager());
    Bukkit.getPluginManager().registerEvents(new DoorListener(), this);
  }

  public void initDB() {
    Database db = new Database();
    db.setData(CONFIG.SOUND_KNOCK_WOOD, "ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR");
    db.setData(CONFIG.SOUND_KNOCK_IRON, "ENTITY_ZOMBIE_ATTACK_IRON_DOOR");
    db.setData(CONFIG.SOUND_KNOCK_VOLUME, 1.0);
    db.setData(CONFIG.SOUND_KNOCK_PITCH, 1.0);
    db.setData(CONFIG.SOUND_KNOCK_CATEGORY, "BLOCKS");
    db.setData(CONFIG.ALLOW_KNOCKING, true);
    db.setData(CONFIG.ALLOW_KNOCKING_TRAPDOORS, true);
    db.setData(CONFIG.ALLOW_KNOCKING_GATES, true);
    db.setData(CONFIG.ALLOW_AUTOCLOSE, true);
    db.setData(CONFIG.KNOCKING_REQUIRES_SHIFT, false);
    db.setData(CONFIG.KNOCKING_REQUIRES_EMPTY_HAND, false);
    db.setData(CONFIG.ALLOW_DOUBLEDOORS, true);
    db.setData(CONFIG.ALLOW_IRONDOORS, true);
    db.setData(CONFIG.AUTOCLOSE_DELAY, 6.0);
  }
}