package org.xodium.doorsplus.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.xodium.doorsplus.DoorsPlus;

public class Config {
    // Sound settings
    public static final String SOUND_KNOCK_WOOD = "sound-knock-wood";
    public static final String SOUND_KNOCK_IRON = "sound-knock-iron";
    public static final String SOUND_KNOCK_VOLUME = "sound-knock-volume";
    public static final String SOUND_KNOCK_PITCH = "sound-knock-pitch";
    public static final String SOUND_KNOCK_CATEGORY = "sound-knock-category";

    // Behavior settings
    public static final String CHECK_FOR_REDSTONE = "check-for-redstone";
    public static final String ALLOW_KNOCKING = "allow-knocking";
    public static final String ALLOW_KNOCKING_TRAPDOORS = "allow-knocking-trapdoors";
    public static final String ALLOW_KNOCKING_GATES = "allow-knocking-gates";
    public static final String ALLOW_AUTOCLOSE = "allow-autoclose";
    public static final String KNOCKING_REQUIRES_SHIFT = "knocking-requires-shift";
    public static final String KNOCKING_REQUIRES_EMPTY_HAND = "knocking-requires-empty-hand";
    public static final String ALLOW_DOUBLEDOORS = "allow-doubledoors";
    public static final String ALLOW_IRONDOORS = "allow-opening-irondoors-with-hands";

    // Auto-close settings
    public static final String AUTOCLOSE_DELAY = "autoclose-delay";

    public static void init() {
        FileConfiguration conf = DoorsPlus.getInstance().getConfig();
        conf.addDefault(CHECK_FOR_REDSTONE, true);
        conf.addDefault(ALLOW_DOUBLEDOORS, true);
        conf.addDefault(SOUND_KNOCK_IRON, "ENTITY_ZOMBIE_ATTACK_IRON_DOOR");
        conf.addDefault(SOUND_KNOCK_WOOD, "ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR");
        conf.addDefault(SOUND_KNOCK_CATEGORY, "BLOCKS");
        conf.addDefault(SOUND_KNOCK_VOLUME, 1.0);
        conf.addDefault(SOUND_KNOCK_PITCH, 1.0);
        conf.addDefault(ALLOW_KNOCKING, true);
        conf.addDefault(ALLOW_KNOCKING_TRAPDOORS, true);
        conf.addDefault(ALLOW_KNOCKING_GATES, true);
        conf.addDefault(KNOCKING_REQUIRES_EMPTY_HAND, false);
        conf.addDefault(ALLOW_AUTOCLOSE, true);
        conf.addDefault(KNOCKING_REQUIRES_SHIFT, false);
        conf.addDefault(AUTOCLOSE_DELAY, 5.0);
    }
}
