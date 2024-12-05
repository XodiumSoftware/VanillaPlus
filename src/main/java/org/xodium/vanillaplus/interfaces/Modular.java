package org.xodium.vanillaplus.interfaces;

import java.util.Map;

public interface Modular {
    String ENABLE = ".enable";

    boolean isEnabled();

    Map<String, Object> config();
}