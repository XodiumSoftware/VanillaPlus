package org.xodium.vanillaplus;

import java.util.List;
import java.util.function.Supplier;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.xodium.vanillaplus.modules.DoorsModule;
import org.xodium.vanillaplus.modules.RecipesModule;

public class ModuleManager {
    private final VanillaPlus vp = VanillaPlus.getInstance();
    private final ConfigManager cm = new ConfigManager();
    private final PluginManager pm = vp.getServer().getPluginManager();
    private final static String MODULE_LOADED_MSG = "Loaded: %s";
    private final List<Supplier<? extends Listener>> modules = List.of(
            DoorsModule::new,
            RecipesModule::new);

    {
        modules.forEach(supplier -> {
            if (cm.getData(supplier.get().getClass().getSimpleName() + ".enable").getAsBoolean()) {
                pm.registerEvents((Listener) supplier.get(), vp);
                vp.getLogger().info(String.format(MODULE_LOADED_MSG, supplier.get().getClass().getSimpleName()));
            }
        });
    }
}
