package org.xodium.vanillaplus.interfaces;

import org.bukkit.event.Listener;

public interface Modular extends Listener {
    interface CONFIG {
        String ENABLE = ".enable";
    }

    boolean enabled();

    void config();
}