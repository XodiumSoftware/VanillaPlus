package org.xodium.vanillaplus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.xodium.vanillaplus.commands.ReloadCommand;
import org.xodium.vanillaplus.modules.DoorsModule;
import org.xodium.vanillaplus.modules.RecipesModule;

public class ModuleManager {
    private final VanillaPlus vp = VanillaPlus.getInstance();
    private final ConfigManager cm = new ConfigManager();
    private final PluginManager pm = vp.getServer().getPluginManager();
    private final static String MODULE_LOADED_MSG = "Loaded %s module";

    {
        ReloadCommand.init(vp.getLifecycleManager());
        if (cm.node(DoorsModule.ENABLE).getBoolean()) {
            pm.registerEvents(new DoorsModule(), vp);
            vp.getLogger().info(String.format(MODULE_LOADED_MSG, DoorsModule.class.getSimpleName()));
        }
        if (cm.node(RecipesModule.ENABLE).getBoolean()) {
            pm.registerEvents(new RecipesModule(), vp);
            vp.getLogger().info(String.format(MODULE_LOADED_MSG, RecipesModule.class.getSimpleName()));
        }
    }
}
