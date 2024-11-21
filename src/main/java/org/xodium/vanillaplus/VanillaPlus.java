package org.xodium.vanillaplus;

import java.util.Arrays;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.xodium.vanillaplus.commands.GUICommand;
import org.xodium.vanillaplus.interfaces.CONFIG;
import org.xodium.vanillaplus.listeners.DoorListener;
import org.xodium.vanillaplus.listeners.GUIListener;
import org.xodium.vanillaplus.listeners.ToolListener;
import org.xodium.vanillaplus.managers.ItemManager;

public class VanillaPlus extends JavaPlugin {

  private static VanillaPlus instance;
  private static final String[] V = { "1.21.1", "1.21.3" };

  public static VanillaPlus getInstance() {
    return instance;
  }

  @Override
  public void onEnable() {
    if (Arrays.stream(V).noneMatch(version -> getServer().getVersion().contains(version))) {
      getLogger().severe("This plugin requires Paper version: " + String.join(", ", V));
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    instance = this;
    initDB();
    initManagers();
    initCmds();
    initEvents();
  }

  private void initEvents() {
    PluginManager pm = getServer().getPluginManager();
    pm.registerEvents(new DoorListener(), this);
    pm.registerEvents(new ToolListener(), this);
    pm.registerEvents(new GUIListener(), this);
  }

  private void initCmds() {
    GUICommand.init(this.getLifecycleManager());
  }

  private void initManagers() {
    ItemManager im = new ItemManager();
    im.createChisel();
  }

  private void initDB() {
    Database db = new Database();
    db.setData(CONFIG.SOUND_KNOCK_WOOD, "ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR");
    db.setData(CONFIG.SOUND_KNOCK_VOLUME, 1.0);
    db.setData(CONFIG.SOUND_KNOCK_PITCH, 1.0);
    db.setData(CONFIG.SOUND_KNOCK_CATEGORY, "BLOCKS");
    db.setData(CONFIG.ALLOW_KNOCKING, true);
    db.setData(CONFIG.ALLOW_KNOCKING_TRAPDOORS, true);
    db.setData(CONFIG.ALLOW_KNOCKING_GATES, true);
    db.setData(CONFIG.ALLOW_AUTOCLOSE, true);
    db.setData(CONFIG.KNOCKING_REQUIRES_SHIFT, false);
    db.setData(CONFIG.KNOCKING_REQUIRES_EMPTY_HAND, true);
    db.setData(CONFIG.ALLOW_DOUBLEDOORS, true);
    db.setData(CONFIG.AUTOCLOSE_DELAY, 6);
  }
}