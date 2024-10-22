package org.xodium.doorsplus;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.xodium.doorsplus.commands.GUICommand;
import org.xodium.doorsplus.interfaces.CONFIG;
import org.xodium.doorsplus.interfaces.CONST;
import org.xodium.doorsplus.listeners.DoorListener;

public class DoorsPlus extends JavaPlugin {

  private static DoorsPlus instance;

  public static DoorsPlus getInstance() {
    return instance;
  }

  @Override
  public void onEnable() {
    if (!getServer().getVersion().contains(CONST.V)) {
      getLogger().severe("This plugin requires paper version: " + CONST.V);
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
    db.setData(CONFIG.AUTOCLOSE_DELAY, 5.0);
  }
}