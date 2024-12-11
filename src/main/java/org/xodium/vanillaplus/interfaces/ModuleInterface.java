package org.xodium.vanillaplus.interfaces;

import org.bukkit.event.Listener;

/**
 * The ModuleInterface defines the structure for modules that can be enabled or
 * configured.
 * It extends the Listener interface, indicating that implementing classes can
 * handle events.
 */
public interface ModuleInterface extends Listener {
    static interface CONFIG {
        String ENABLE = ".enable";
    }

    boolean enabled();

    void config();
}