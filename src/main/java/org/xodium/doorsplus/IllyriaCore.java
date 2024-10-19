package org.xodium.doorsplus;

import java.io.InputStream;
import java.util.Set;
import java.io.InputStreamReader;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.xodium.doorsplus.interfaces.CONST;
import org.xodium.doorsplus.interfaces.MSG;

public class IllyriaCore extends JavaPlugin {

  @Override
  public void onEnable() {
    if (!IllyriaUtils.isCompatibleEnv(this)) {
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    loadConfig();
    registerEvents();

    getLogger().info(MSG.ILLYRIA_CORE_ENABLED);
  }

  @Override
  public void onDisable() {
    getLogger().info(MSG.ILLYRIA_CORE_DISABLED);
  }

  // TODO: the method still adds content to the top-level keys, even tho it
  // shouldnt.
  private void loadConfig() {
    FileConfiguration config = getConfig();
    config.options().copyDefaults(true);
    InputStream defaultConfigStream = getResource(CONST.CONFIG_FILE);

    if (defaultConfigStream != null) {
      FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));
      Set<String> keys = defaultConfig.getKeys(false);

      for (String key : keys) {
        if (!config.contains(key)) {
          config.set(key, defaultConfig.get(key));
        }
      }
    }
    saveConfig();
  }

  private void registerEvents() {
  }
}