package org.xodium.illyriacore;

import java.io.InputStream;
import java.util.Set;
import java.io.InputStreamReader;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.xodium.illyriacore.interfaces.CONST;
import org.xodium.illyriacore.interfaces.MSG;
import org.xodium.illyriacore.listeners.EventListener;

import net.luckperms.api.LuckPerms;

public class IllyriaCore extends JavaPlugin {
  private LuckPerms lp;

  @Override
  public void onEnable() {
    if (!IllyriaUtils.isCompatibleEnv(this)) {
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
    if (provider != null) {
      this.lp = provider.getProvider();
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
    getServer().getPluginManager().registerEvents(new EventListener(IllyriaUtils.loadFromConfig(this), lp), this);
  }
}