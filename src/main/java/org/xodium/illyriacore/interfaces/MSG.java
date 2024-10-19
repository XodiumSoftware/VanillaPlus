package org.xodium.illyriacore.interfaces;

public interface MSG {
    String ILLYRIA_CORE_DISABLED = ASCII.RED + "IllyriaCore disabled!";
    String ILLYRIA_CORE_ENABLED = ASCII.GREEN + "IllyriaCore enabled!";
    String WRONG_VERSION = "This plugin requires " + DEP.V + "!";
    String LP_MISSING = DEP.LP + " plugin not found! This plugin requires " + DEP.LP;
    String INVALID_ENTITY_TYPE_OR_PERM_FOR_KEY = "Invalid entity type or permission for key: ";
}
