package org.xodium.vanillaplus;

import org.xodium.vanillaplus.interfaces.MSG;
import org.xodium.vanillaplus.interfaces.Modular;
import org.xodium.vanillaplus.modules.DoorsModule;

import java.util.List;

public class ModuleManager {
    private final VanillaPlus vp = VanillaPlus.getInstance();

    {
        // TODO: make it automaticall load all modules in the modules folder.
        List.of(new DoorsModule())
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