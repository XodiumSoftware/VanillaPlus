package org.xodium.vanillaplus;

import org.xodium.vanillaplus.VanillaPlus.MSG;
import org.xodium.vanillaplus.interfaces.Modular;
import org.xodium.vanillaplus.modules.DoorsModule;

import java.util.List;

public class ModuleManager implements MSG {
    private final VanillaPlus vp = VanillaPlus.getInstance();

    {
        List.of(new DoorsModule())
                .stream()
                .peek(Modular::config)
                .filter(Modular::isEnabled)
                .forEach(module -> {
                    vp.getServer().getPluginManager().registerEvents(module, vp);
                    vp.getLogger().info(PREFIX + "Loaded: " + module.getClass().getSimpleName());
                });
    }
}