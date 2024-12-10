package org.xodium.vanillaplus;

import org.xodium.vanillaplus.interfaces.ModuleInterface;
import org.xodium.vanillaplus.modules.DoorsModule;

import java.util.List;

public class ModuleManager {
    private final VanillaPlus vp = VanillaPlus.getInstance();

    {
        List.of(new DoorsModule())
                .stream()
                .peek(ModuleInterface::config)
                .filter(ModuleInterface::enabled)
                .forEach(module -> {
                    vp.getServer().getPluginManager().registerEvents(module, vp);
                    vp.getLogger().info("Loaded: " + module.getClass().getSimpleName());
                });
    }
}