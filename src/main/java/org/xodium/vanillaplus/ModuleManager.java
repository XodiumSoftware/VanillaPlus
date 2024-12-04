package org.xodium.vanillaplus;

import java.util.Map;
import java.util.function.Supplier;

import org.bukkit.plugin.PluginManager;
import org.xodium.vanillaplus.modules.DoorsModule;
import org.xodium.vanillaplus.modules.RecipesModule;

public class ModuleManager {
    private final VanillaPlus vp = VanillaPlus.getInstance();
    private final ConfigManager cm = new ConfigManager();
    private final PluginManager pm = vp.getServer().getPluginManager();
    private final static String MODULE_LOADED_MSG = "Loaded: %s";
    private final Map<String, Supplier<?>> modules = Map.of(
            DoorsModule.ENABLE, DoorsModule::new,
            RecipesModule.ENABLE, RecipesModule::new);

    {
        modules.forEach((key, supplier) -> {
            if (cm.getData(key).getAsBoolean()) {
                pm.registerEvents((org.bukkit.event.Listener) supplier.get(), vp);
                vp.getLogger().info(String.format(MODULE_LOADED_MSG, supplier.get().getClass().getSimpleName()));
            }
        });
    }
}
