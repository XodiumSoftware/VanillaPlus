package org.xodium.vanillaplus;

import org.xodium.vanillaplus.interfaces.MSG;
import org.xodium.vanillaplus.interfaces.Modular;
import org.xodium.vanillaplus.modules.DoorsModule;
import org.xodium.vanillaplus.modules.RecipesModule;

import java.util.List;

public class ModuleManager {
    private final VanillaPlus vp = VanillaPlus.getInstance();

    {
        List.of(new RecipesModule(), new DoorsModule())
                .stream()
                // TODO: make the config actually work.
                .peek(Modular::config)
                .filter(Modular::isEnabled)
                .forEach(module -> {
                    vp.getServer().getPluginManager().registerEvents(module, vp);
                    // TODO: make it show a shortified version of the module name.
                    vp.getLogger().info(MSG.MODULE_LOADED + module);
                });
    }
}