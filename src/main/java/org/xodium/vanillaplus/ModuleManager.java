package org.xodium.vanillaplus;

import org.xodium.vanillaplus.interfaces.ModuleInterface;
import org.xodium.vanillaplus.modules.DoorsModule;
import org.xodium.vanillaplus.modules.SaplingModule;

import java.util.stream.Stream;

public class ModuleManager {
    private static final VanillaPlus VP = VanillaPlus.getInstance();

    static {
        Stream.of(new DoorsModule(), new SaplingModule())
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