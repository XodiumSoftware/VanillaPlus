package org.xodium.doorsplus;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.xodium.doorsplus.commands.ReloadCommand;
import org.xodium.doorsplus.config.Config;
import org.xodium.doorsplus.interfaces.CONST;
import org.xodium.doorsplus.listeners.DoorListener;

public class DoorsPlus extends JavaPlugin {

  private static DoorsPlus instance;
  private boolean redstoneEnabled = false;

  public static DoorsPlus getInstance() {
    return instance;
  }

  public boolean isRedstoneEnabled() {
    return redstoneEnabled;
  }

  @Override
  public void onEnable() {
    if (!getServer().getVersion().contains(CONST.V)) {
      getLogger().severe("This plugin requires paper version: " + CONST.V);
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    ReloadCommand.init(this.getLifecycleManager());
    instance = this;
    new Database();
    Config.init();
    reload();
    Bukkit.getPluginManager().registerEvents(new DoorListener(), this);
  }

  public void reload() {
    saveDefaultConfig();
    reloadConfig();
    redstoneEnabled = getConfig().getBoolean(Config.CHECK_FOR_REDSTONE);
  }
}