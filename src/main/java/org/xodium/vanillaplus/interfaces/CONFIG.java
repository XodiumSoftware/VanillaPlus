package org.xodium.vanillaplus.interfaces;

public interface CONFIG {
    String SETTINGS = "settings.";

    interface DoorsPlus {
        String PREFIX = SETTINGS + "doorsplus.";
        String ENABLE = PREFIX + "enable";

        // Sound settings
        String SOUND_KNOCK_CATEGORY = PREFIX + "sound_knock_category";
        String SOUND_KNOCK_PITCH = PREFIX + "sound_knock_pitch";
        String SOUND_KNOCK_VOLUME = PREFIX + "sound_knock_volume";
        String SOUND_KNOCK_WOOD = PREFIX + "sound_knock_wood";

        // Behavior settings
        String ALLOW_AUTOCLOSE = PREFIX + "allow_autoclose";
        String ALLOW_DOUBLEDOORS = PREFIX + "allow_doubledoors";
        String ALLOW_KNOCKING = PREFIX + "allow_knocking";
        String ALLOW_KNOCKING_GATES = PREFIX + "allow_knocking_gates";
        String ALLOW_KNOCKING_TRAPDOORS = PREFIX + "allow_knocking_trapdoors";
        String KNOCKING_REQUIRES_EMPTY_HAND = PREFIX + "knocking_requires_empty_hand";
        String KNOCKING_REQUIRES_SHIFT = PREFIX + "knocking_requires_shift";

        // Auto-close settings
        String AUTOCLOSE_DELAY = PREFIX + "autoclose_delay";
    }

    interface RecipesPlus {
        String PREFIX = SETTINGS + "recipesplus.";
        String ENABLE = PREFIX + "enable";
    }

    interface ElevatorPlus {
        String PREFIX = SETTINGS + "elevatorplus.";
        String ENABLE = PREFIX + "enable";
    }
}