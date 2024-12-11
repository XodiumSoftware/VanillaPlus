package org.xodium.vanillaplus;

import org.xodium.vanillaplus.interfaces.ModuleInterface;
import org.xodium.vanillaplus.modules.DoorsModule;

import java.util.List;

public class ModuleManager {
    private static final VanillaPlus VP = VanillaPlus.getInstance();

    static {
        List.of(new DoorsModule())
                .stream()
                .peek(ModuleInterface::config)
                .filter(ModuleInterface::enabled)
                .forEach(mod -> {
                    long startTime = System.currentTimeMillis();
                    VP.getServer().getPluginManager().registerEvents(mod, VP);
                    long endTime = System.currentTimeMillis();
                    VP.getLogger()
                            .info("Loaded: " + mod.getClass().getSimpleName() + "| Took " + (endTime - startTime)
                                    + "ms");
                });
    }
}