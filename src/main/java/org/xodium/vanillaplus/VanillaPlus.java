package org.xodium.vanillaplus;

import java.util.Arrays;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.xodium.vanillaplus.commands.ReloadCommand;
import org.xodium.vanillaplus.listeners.DoorListener;

public class VanillaPlus extends JavaPlugin {

  private static VanillaPlus instance;
  private static final String[] V = { "1.21.3" };

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
    saveDefaultConfig();
    initCmds();
    initEvents();
  }

  private void initEvents() {
    PluginManager pm = getServer().getPluginManager();
    pm.registerEvents(new DoorListener(), this);
  }

  private void initCmds() {
    ReloadCommand.init(this.getLifecycleManager());
  }
}