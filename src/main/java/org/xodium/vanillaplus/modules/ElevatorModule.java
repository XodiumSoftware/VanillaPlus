package org.xodium.vanillaplus.modules;

import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.interfaces.Modular;

public class ElevatorModule implements Modular {
    private final VanillaPlus vp = VanillaPlus.getInstance();
    private final FileConfiguration fc = vp.getConfig();
    private final String className = getClass().getSimpleName();

    @Override
    public boolean isEnabled() {
        return fc.getBoolean(className + ENABLE);
    }

    @Override
    public void config() {
        fc.addDefaults(Map.ofEntries(
                Map.entry(className + ENABLE, true)));
        vp.saveConfig();
    }

}
