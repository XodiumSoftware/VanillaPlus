package org.xodium.vanillaplus;

import org.bukkit.plugin.java.JavaPlugin;
import org.xodium.vanillaplus.commands.ReloadCommand;

import java.util.Arrays;

public class VanillaPlus extends JavaPlugin {
    // TODO: revert back to using the build in config system instead of a database.

    private static final String[] V = { "1.21.1", "1.21.3", "1.21.4" };
    private static final String[] PAPER = { "Paper" };
    private static final String IS_PAPER_MSG = "This plugin is not compatible with non-Paper servers.";
    private static final String IS_SUPPORTED_VERSION_MSG = "This plugin requires Paper version(s): "
            + String.join(", ", V);

    public static final String PREFIX = "<gold>[<dark_aqua>VanillaPlus<gold>] <reset>";

    public static VanillaPlus getInstance() {
        return getPlugin(VanillaPlus.class);
    }

    @Override
    public void onEnable() {
        if (!isPaper()) {
            disablePlugin(IS_PAPER_MSG);
            return;
        }
        if (!isSupportedVersion()) {
            disablePlugin(IS_SUPPORTED_VERSION_MSG);
            return;
        }
        saveDefaultConfig();
        new ReloadCommand();
        new ModuleManager();
    }

    private void disablePlugin(String msg) {
        getLogger().severe(msg);
        getServer().getPluginManager().disablePlugin(this);
    }

    private boolean isSupportedVersion() {
        return Arrays.stream(V)
                .anyMatch(v -> getServer().getVersion().contains(v));
    }

    private boolean isPaper() {
        return Arrays.stream(PAPER)
                .anyMatch(v -> getServer().getName().contains(v));
    }
}