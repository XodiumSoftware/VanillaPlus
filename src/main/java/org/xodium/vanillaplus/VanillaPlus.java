package org.xodium.vanillaplus;

import java.util.Arrays;
import org.bukkit.plugin.java.JavaPlugin;
import org.xodium.vanillaplus.commands.ReloadCommand;

/**
 * Retrieves the singleton instance of the VanillaPlus plugin.
 *
 * @return the current instance of VanillaPlus
 */
public class VanillaPlus extends JavaPlugin {

    private static final String[] V = { "1.21.1", "1.21.3", "1.21.4" };
    private static final String[] PAPER = { "Paper" };
    private static final String IS_PAPER_MSG = "This plugin is not compatible with non-Paper servers.";
    private static final String IS_SUPPORTED_VERSION_MSG = "This plugin requires Paper version(s): "
            + String.join(", ", V);

    public static final String PREFIX = "<gold>[<dark_aqua>VanillaPlus<gold>] <reset>";
    public static final String MM_HEX_PREFIX = "<#CB2D3E>";

    public static VanillaPlus getInstance() {
        return getPlugin(VanillaPlus.class);
    }

    /**
     * This method is called when the plugin is enabled.
     * It performs the following actions:
     * 1. Checks if the server is running Paper. If not, disables the plugin with a
     * message.
     * 2. Checks if the server is running a supported version. If not, disables the
     * plugin with a message.
     * 3. Initializes the ReloadCommand.
     * 4. Initializes the ModuleManager.
     */
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
        new ReloadCommand();
        new ModuleManager();
    }

    /**
     * Disables the plugin and logs a severe message.
     *
     * @param msg the message to log before disabling the plugin
     */
    private void disablePlugin(String msg) {
        getLogger().severe(msg);
        getServer().getPluginManager().disablePlugin(this);
    }

    /**
     * Checks if the current server version is supported.
     *
     * This method streams through the array of supported versions (V) and checks
     * if the server's version string contains any of the supported version strings.
     *
     * @return true if the server version is supported, false otherwise.
     */
    private boolean isSupportedVersion() {
        return Arrays.stream(V)
                .anyMatch(v -> getServer().getVersion().contains(v));
    }

    /**
     * Checks if the server is running on Paper.
     *
     * This method streams through the predefined array of Paper server names
     * and checks if any of them are contained in the server's name.
     *
     * @return true if the server is identified as a Paper server, false otherwise.
     */
    private boolean isPaper() {
        return Arrays.stream(PAPER)
                .anyMatch(v -> getServer().getName().contains(v));
    }
}