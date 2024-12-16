package org.xodium.vanillaplus.modules;

import org.bukkit.configuration.file.FileConfiguration;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.interfaces.ModuleInterface;

public class SaplingModule implements ModuleInterface {
    private final String cn = getClass().getSimpleName();
    private static final VanillaPlus VP = VanillaPlus.getInstance();
    private static final FileConfiguration FC = VP.getConfig();

    @Override
    public boolean enabled() {
        return FC.getBoolean(cn + CONFIG.ENABLE);
    }

    @Override
    public void config() {
        FC.addDefault(cn + CONFIG.ENABLE, true);
        VP.saveConfig();
    }

}
