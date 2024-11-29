package org.xodium.vanillaplus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.xodium.vanillaplus.commands.ReloadCommand;
import org.xodium.vanillaplus.interfaces.CONFIG;
import org.xodium.vanillaplus.listeners.DoorListener;
import org.xodium.vanillaplus.modules.RecipesPlusModule;

public class ModuleManager {
    private final VanillaPlus vp = VanillaPlus.getInstance();
    private final FileConfiguration fc = vp.getConfig();
    private final PluginManager pm = vp.getServer().getPluginManager();

    {
        ReloadCommand.init(vp.getLifecycleManager());
        if (fc.getBoolean(CONFIG.DoorsPlus.ENABLE)) {
            pm.registerEvents(new DoorListener(), vp);
            vp.getLogger().info("DoorsPlus is enabled");
        }
        if (fc.getBoolean(CONFIG.RecipesPlus.ENABLE)) {
            pm.registerEvents(new RecipesPlusModule(), vp);
            vp.getLogger().info("RecipesPlus is enabled");
        }
    }
}
