package org.xodium.vanillaplus.interfaces;

public interface Modular {
    String ENABLE = ".enable";

    boolean isEnabled();

    void config();
}