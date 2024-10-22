package org.xodium.vanillaplus.interfaces;

public interface CONFIG {
    // Sound settings
    String SOUND_KNOCK_WOOD = "sound-knock-wood";
    String SOUND_KNOCK_IRON = "sound-knock-iron";
    String SOUND_KNOCK_VOLUME = "sound-knock-volume";
    String SOUND_KNOCK_PITCH = "sound-knock-pitch";
    String SOUND_KNOCK_CATEGORY = "sound-knock-category";

    // Behavior settings
    String ALLOW_KNOCKING = "allow-knocking";
    String ALLOW_KNOCKING_TRAPDOORS = "allow-knocking-trapdoors";
    String ALLOW_KNOCKING_GATES = "allow-knocking-gates";
    String ALLOW_AUTOCLOSE = "allow-autoclose";
    String KNOCKING_REQUIRES_SHIFT = "knocking-requires-shift";
    String KNOCKING_REQUIRES_EMPTY_HAND = "knocking-requires-empty-hand";
    String ALLOW_DOUBLEDOORS = "allow-doubledoors";
    String ALLOW_IRONDOORS = "allow-opening-irondoors-with-hands";

    // Auto-close settings
    String AUTOCLOSE_DELAY = "autoclose-delay";
}
