package org.xodium.vanillaplus.modules;

import org.xodium.vanillaplus.Database;
import org.xodium.vanillaplus.interfaces.Modular;

public class ElevatorModule implements Modular {
    private final String cn = getClass().getSimpleName();
    private final Database db = new Database();

    @Override
    public boolean enabled() {
        return db.getData(cn + CONFIG.ENABLE, Boolean.class);
    }

    @Override
    public void config() {
        db.setData(cn + CONFIG.ENABLE, true);
    }
}
