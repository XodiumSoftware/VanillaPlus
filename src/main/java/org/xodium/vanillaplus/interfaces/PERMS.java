package org.xodium.vanillaplus.interfaces;

// TODO: refactor to each module for modularity.
public interface PERMS {
    String VANILLAPLUS = "vanillaplus.";
    String RELOAD = VANILLAPLUS + "reload";

    interface DOORSMODULE {
        String USE = VANILLAPLUS + "doubledoors";
        String KNOCK = VANILLAPLUS + "knock";
        String AUTOCLOSE = VANILLAPLUS + "autoclose";
    }
}
