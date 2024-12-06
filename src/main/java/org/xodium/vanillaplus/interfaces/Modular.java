package org.xodium.vanillaplus.interfaces;

import org.bukkit.event.Listener;

public interface Modular extends Listener {
    String ENABLE = ".enable";

    boolean isEnabled();

    void config();
}