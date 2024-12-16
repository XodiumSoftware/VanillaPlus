package org.xodium.vanillaplus.interfaces;

import org.bukkit.event.Listener;

public interface ModuleInterface extends Listener {
    interface CONFIG {
        String ENABLE = ".enable";
    }

    boolean enabled();

    void config();
}