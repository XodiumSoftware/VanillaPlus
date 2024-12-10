package org.xodium.vanillaplus;

import org.xodium.vanillaplus.interfaces.Modular;
import org.xodium.vanillaplus.modules.DoorsModule;

import java.util.List;

public class ModuleManager {
    private final VanillaPlus vp = VanillaPlus.getInstance();

    {
        List.of(new DoorsModule())
                .stream()
                .peek(Modular::config)
                .filter(Modular::enabled)
                .forEach(module -> {
                    vp.getServer().getPluginManager().registerEvents(module, vp);
                    vp.getLogger().info(VanillaPlus.PREFIX + "Loaded: " + module.getClass().getSimpleName());
                });
    }
}