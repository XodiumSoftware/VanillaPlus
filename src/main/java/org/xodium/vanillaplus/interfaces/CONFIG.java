package org.xodium.vanillaplus.interfaces;

public interface CONFIG {
    // Sound settings
    String SOUND_KNOCK_CATEGORY = "sound_knock_category";
    String SOUND_KNOCK_PITCH = "sound_knock_pitch";
    String SOUND_KNOCK_VOLUME = "sound_knock_volume";
    String SOUND_KNOCK_WOOD = "sound_knock_wood";

    // Behavior settings
    String ALLOW_AUTOCLOSE = "allow_autoclose";
    String ALLOW_DOUBLEDOORS = "allow_doubledoors";
    String ALLOW_KNOCKING = "allow_knocking";
    String ALLOW_KNOCKING_GATES = "allow_knocking_gates";
    String ALLOW_KNOCKING_TRAPDOORS = "allow_knocking_trapdoors";
    String KNOCKING_REQUIRES_EMPTY_HAND = "knocking_requires_empty_hand";
    String KNOCKING_REQUIRES_SHIFT = "knocking_requires_shift";

    // Auto-close settings
    String AUTOCLOSE_DELAY = "autoclose_delay";
}