package org.xodium.vanillaplus.modules;

import org.xodium.vanillaplus.interfaces.ModuleInterface;

public class SaplingModule implements ModuleInterface {
    private final String cn = getClass().getSimpleName();

    @Override
    public boolean enabled() {
        return DB.getData(cn + CONFIG.ENABLE, Boolean.class);
    }

    @Override
    public void config() {
        DB.setData(cn + CONFIG.ENABLE, true);
    }

}
