package org.xodium.vanillaplus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.xodium.vanillaplus.commands.ReloadCommand;
import org.xodium.vanillaplus.interfaces.CONFIG;
import org.xodium.vanillaplus.modules.DoorsPlusModule;
import org.xodium.vanillaplus.modules.RecipesPlusModule;

public class ModuleManager {
    private final VanillaPlus vp = VanillaPlus.getInstance();
    private final FileConfiguration fc = vp.getConfig();
    private final PluginManager pm = vp.getServer().getPluginManager();
    private final static String MODULE_LOADED_MSG = "Loaded %s module";

    {
        ReloadCommand.init(vp.getLifecycleManager());
        if (fc.getBoolean(CONFIG.DoorsPlus.ENABLE)) {
            pm.registerEvents(new DoorsPlusModule(), vp);
            vp.getLogger().info(String.format(MODULE_LOADED_MSG, "DoorsPlus"));
        }
        if (fc.getBoolean(CONFIG.RecipesPlus.ENABLE)) {
            pm.registerEvents(new RecipesPlusModule(), vp);
            vp.getLogger().info(String.format(MODULE_LOADED_MSG, "RecipesPlus"));
        }
    }
}
