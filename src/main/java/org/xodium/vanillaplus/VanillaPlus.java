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
    if (!isPaper()) {
      getLogger().severe("This plugin is not compatible with non-Paper servers.");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    if (isSupportedVersion()) {
      getLogger().severe("This plugin requires Paper version: " + String.join(", ", V));
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    if (isFolia()) {
      getLogger().severe("This plugin is not compatible with Folia.");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    saveDefaultConfig();
    new ConfigManager();
    new ModuleManager();
  }

  private boolean isSupportedVersion() {
    return Arrays.stream(V)
        .anyMatch(v -> getServer().getVersion().contains(v));
  }

  private boolean isPaper() {
    return getServer().getVersion().contains("Paper");
  }

  private static boolean isFolia() {
    try {
      Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}