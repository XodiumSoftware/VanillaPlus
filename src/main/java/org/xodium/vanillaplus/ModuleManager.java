package org.xodium.vanillaplus;

import org.xodium.vanillaplus.interfaces.ModuleInterface;
import org.xodium.vanillaplus.modules.DoorsModule;

import java.util.List;

/**
 * The ModuleManager class is responsible for initializing and managing modules
 * in the VanillaPlus plugin.
 * It registers the modules with the server's plugin manager and logs their
 * loading status.
 * 
 * <p>
 * This class uses a static initializer block to:
 * <ul>
 * <li>Create a list of modules (currently only DoorsModule).</li>
 * <li>Configure each module using the {@code config} method of
 * {@code ModuleInterface}.</li>
 * <li>Filter the modules to include only those that are enabled.</li>
 * <li>Register the enabled modules with the server's plugin manager and log
 * their loading status.</li>
 * </ul>
 * 
 * <p>
 * Dependencies:
 * <ul>
 * <li>{@code VanillaPlus} - The main plugin instance.</li>
 * <li>{@code ModuleInterface} - Interface that modules implement.</li>
 * <li>{@code DoorsModule} - Example module being managed.</li>
 * </ul>
 */
public class ModuleManager {
    private static final VanillaPlus VP = VanillaPlus.getInstance();

    static {
        List.of(new DoorsModule())
                .stream()
                .peek(ModuleInterface::config)
                .filter(ModuleInterface::enabled)
                .forEach(mod -> {
                    VP.getServer().getPluginManager().registerEvents(mod, VP);
                    VP.getLogger().info("Loaded: " + mod.getClass().getSimpleName());
                });
    }
}