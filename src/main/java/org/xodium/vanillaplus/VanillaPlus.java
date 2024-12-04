package org.xodium.vanillaplus;

import java.util.Arrays;
import org.bukkit.plugin.java.JavaPlugin;
import org.xodium.vanillaplus.commands.ReloadCommand;

public class VanillaPlus extends JavaPlugin {

  private static final String FOLIA = "io.papermc.paper.threadedregions.RegionizedServer";
  private static final String PAPER = "Paper";
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
    if (!isSupportedVersion()) {
      getLogger().severe("This plugin requires Paper version: " + String.join(", ", V));
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    if (isFolia()) {
      getLogger().severe("This plugin is not compatible with Folia.");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    new ReloadCommand();
    new ModuleManager();
  }

  private boolean isSupportedVersion() {
    return Arrays.stream(V)
        .anyMatch(v -> getServer().getVersion().contains(v));
  }

  private boolean isPaper() {
    return getServer().getName().contains(PAPER);
  }

  private static boolean isFolia() {
    try {
      Class.forName(FOLIA);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}