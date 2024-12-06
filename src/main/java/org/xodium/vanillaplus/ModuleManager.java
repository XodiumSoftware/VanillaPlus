package org.xodium.vanillaplus;

import org.xodium.vanillaplus.interfaces.MSG;
import org.xodium.vanillaplus.modules.DoorsModule;

import java.util.List;

// TODO: make the config actually work.
public class ModuleManager {
    private final VanillaPlus vp = VanillaPlus.getInstance();

    {
        List.of(new DoorsModule())
                .stream()
                .peek(module -> {
                    module.config();
                })
                .filter(module -> {
                    return module.isEnabled();
                })
                .forEach(module -> {
                    vp.getServer().getPluginManager().registerEvents(module, vp);
                    vp.getLogger().info(MSG.MODULE_LOADED + module.getClass().getSimpleName());
                });
    }
}