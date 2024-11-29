package org.xodium.vanillaplus;

import java.util.Arrays;
import org.bukkit.plugin.java.JavaPlugin;

public class VanillaPlus extends JavaPlugin {

  private static final String[] V = { "1.21.3" };

  public static VanillaPlus getInstance() {
    return JavaPlugin.getPlugin(VanillaPlus.class);
  }

  @Override
  public void onEnable() {
    if (Arrays.stream(V).noneMatch(V -> getServer().getVersion().contains(V))) {
      getLogger().severe("This plugin requires Paper version: " + String.join(", ", V));
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    saveDefaultConfig();
    new ModuleManager();
  }
}