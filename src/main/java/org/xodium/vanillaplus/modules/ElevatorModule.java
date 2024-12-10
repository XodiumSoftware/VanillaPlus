package org.xodium.vanillaplus.modules;

import org.xodium.vanillaplus.Database;
import org.xodium.vanillaplus.interfaces.Modular;

public class ElevatorModule implements Modular {
    private final String className = getClass().getSimpleName();
    private final Database db = new Database();

    @Override
    public boolean isEnabled() {
        return (boolean) db.getData(className + ENABLE);
    }

    @Override
    public void config() {
        db.setData(className + ENABLE, true);
    }

}
