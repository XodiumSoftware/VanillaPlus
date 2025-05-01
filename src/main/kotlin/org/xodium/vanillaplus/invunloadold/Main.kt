/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package org.xodium.vanillaplus.invunloadold

import com.jeff_media.jefflib.data.McVersion
import de.jeff_media.InvUnload.*
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

class Main : JavaPlugin(), Listener {
    private val currentConfigVersion = 36
    var useChestSort: Boolean = false
    var messages: Messages? = null
    var usingMatchingConfig: Boolean = true
    protected var blockUtils: BlockUtils? = null
    protected var defaultChestRadius: Int = 10
    protected var maxChestRadius: Int = 20
    protected var chestSortHook: ChestSortHook? = null
    protected var plotSquaredHook: PlotSquaredHook? = null
    protected var inventoryPagesHook: InventoryPagesHook? = null
    protected var visualizer: Visualizer? = null
    protected var groupUtils: GroupUtils? = null
    var coreProtectHook: CoreProtectHook? = null
    var mcVersion: String? = null // 1.13.2 = 1_13_R2

    // 1.14.4 = 1_14_R1
    // 1.8.0 = 1_8_R1
    var mcMinorVersion: Int = 0 // 14 for 1.14, 13 for 1.13, ...
    var commandUnload: CommandUnload? = null
    var commandUnloadInfo: CommandUnloadinfo? = null
    var commandSearchitem: CommandSearchitem? = null
    var commandBlacklist: CommandBlacklist? = null
    var materialTabCompleter: MaterialTabCompleter? = null
    var playerSettings: HashMap<UUID?, PlayerSetting?>? = null
    private var updateChecker: UpdateChecker? = null
    private var enchantmentUtils: EnchantmentUtils? = null

    private var updateCheckInterval = 4.0
    var itemsAdderWrapper: ItemsAdderWrapper? = null
        get() {
            if (field == null) {
                field = ItemsAdderWrapper.init(this)
            }

            return field
        }
        private set

    fun getEnchantmentUtils(): EnchantmentUtils? {
        return enchantmentUtils
    }

    override fun onDisable() {
        saveAllPlayerSettings()
    }

    override fun onEnable() {
        instance = this

        Metrics(this, 3156)

        reloadCompleteConfig(false)

        if (!getConfig().getBoolean("use-chestsort") || Bukkit.getPluginManager().getPlugin("ChestSort") == null) {
            useChestSort = false
        } else {
            try {
                Class.forName("de.jeff_media.chestsort.api.ChestSortAPI")
                useChestSort = true
                logger.info("Succesfully hooked into ChestSort")
            } catch (e: ClassNotFoundException) {
                logger.warning("Your version of ChestSort is too old, disabling ChestSort integration. Please upgrade ChestSort to version 11.0.0 or later.")
            }
        }

        chestSortHook = ChestSortHook(this)
        plotSquaredHook = PlotSquaredHook(this)
        coreProtectHook = CoreProtectHook(this)
        inventoryPagesHook = InventoryPagesHook(this)
        enchantmentUtils = EnchantmentUtils(this)

        registerCommands()
    }

    private fun createConfig() {
        saveResource("groups.example.yml", true)

        // This saves the config.yml included in the .jar file, but it will not
        // overwrite an existing config.yml
        this.saveDefaultConfig()
        reloadConfig()
        //System.out.println("DEBUG: Current config version: "+getConfig().getInt("config-version",0));
        if (getConfig().getInt("config-version", 0) != currentConfigVersion) {
            //System.out.println("DEBUG: Current config version: "+getConfig().getInt("config-version",0));

            showOldConfigWarning()
            var configUpdater: ConfigUpdater? = ConfigUpdater(this)
            configUpdater!!.updateConfig()
            configUpdater = null
            usingMatchingConfig = true
            // createConfig();
        }

        setDefaultConfigValues()
    }

    private fun setDefaultConfigValues() {
        // If you use an old config file with missing options, the following default
        // values will be used instead
        // for every missing option.
        getConfig().addDefault("max-chest-radius", 20)
        maxChestRadius = getConfig().getInt("max-chest-radius")

        getConfig().addDefault("default-chest-radius", 10)
        defaultChestRadius = getConfig().getInt("default-chest-radius")

        getConfig().addDefault("unload-before-dumping", true)

        getConfig().addDefault("check-interval", 4)
        updateCheckInterval = getConfig().getDouble("check-interval")

        getConfig().addDefault("use-chestsort", true)
        getConfig().addDefault("force-chestsort", false)
        getConfig().addDefault("use-itemsadder", true)
        getConfig().addDefault("match-enchantments-on-books", false)
        getConfig().addDefault("match-enchantments", false)

        getConfig().addDefault("use-playerinteractevent", true)
        getConfig().addDefault("use-coreprotect", true)
        getConfig().addDefault("use-plotsquared", true)
        getConfig().addDefault("plotsquared-allow-when-trusted", true)
        getConfig().addDefault("plotsquared-allow-outside-plots", true)

        getConfig().addDefault("spawn-particles", true)
        getConfig().addDefault("particle-type", "WITCH")
        getConfig().addDefault("particle-count", 100)

        getConfig().addDefault("always-show-summary", true)

        getConfig().addDefault("laser-animation", true)
        getConfig().addDefault("laser-default-duration", 5)
        getConfig().addDefault("laser-max-distance", 30)
        getConfig().addDefault("laser-max-distance", 50)
        getConfig().addDefault("laser-moves-with-player", false)

        getConfig().addDefault("strict-tabcomplete", true)

        if (McVersion.current().isAtLeast(1, 20, 6) && getConfig().getString("particle-type", "")
                .equals("WITCH_SPELL", ignoreCase = true)
        ) {
            getConfig().set("particle-type", "WITCH")
        }

        if (!McVersion.current().isAtLeast(1, 20, 6) && getConfig().getString("particle-type", "")
                .equals("WITCH", ignoreCase = true)
        ) {
            getConfig().set("particle-type", "WITCH_SPELL")
        }

        if (!EnumUtils.particleExists(getConfig().getString("particle-type"))) {
            logger.warning("Specified particle type \"" + getConfig().getString("particle-type") + "\" does not exist! Please check your config.yml")
            getConfig().set("error-particles", true)
        }
        if (!EnumUtils.soundExists(getConfig().getString("sound-effect"))) {
            logger.warning("Specified sound effect \"" + getConfig().getString("sound-effect") + "\" does not exist! Please check your config.yml")
            getConfig().set("error-sound", true)
        }
    }

    private fun showOldConfigWarning() {
        logger.warning("==============================================")
        logger.warning("You were using an old config file. InvUnload")
        logger.warning("has updated the file to the newest version.")
        logger.warning("Your changes have been kept.")
        logger.warning("==============================================")
    }

    private fun registerCommands() {
        commandUnload = CommandUnload(this)
        commandUnloadInfo = CommandUnloadinfo(this)
        commandSearchitem = CommandSearchitem(this)
        commandBlacklist = CommandBlacklist(this)
        materialTabCompleter = MaterialTabCompleter(this)
        getCommand("unload")!!.setExecutor(commandUnload)
        getCommand("dump")!!.setExecutor(commandUnload)
        getCommand("unloadinfo")!!.setExecutor(commandUnloadInfo)
        getCommand("searchitem")!!.setExecutor(commandSearchitem)
        getCommand("searchitem")!!.tabCompleter = materialTabCompleter
        getCommand("blacklist")!!.setExecutor(commandBlacklist)
        getCommand("blacklist")!!.tabCompleter = commandBlacklist
    }

    private fun initUpdateChecker() {
        if (updateChecker != null) updateChecker.stop()

        // Check for updates (async, of course)
        // When set to true, we check for updates right now, and every X hours (see
        // updateCheckInterval)
        updateChecker =
            UpdateChecker(this, UpdateCheckSource.CUSTOM_URL, "https://api.jeff-media.com/invunload/latest-version.txt")
                .suppressUpToDateMessage(true)
                .setDonationLink("https://paypal.me/mfnalex")
                .setDownloadLink(60095)
                .setChangelogLink(60095)
        if (getConfig().getString("check-for-updates", "true").equals("true", ignoreCase = true)) {
            updateChecker.checkNow().checkEveryXHours(updateCheckInterval)
        } // When set to on-startup, we check right now (delay 0)
        else if (getConfig().getString("check-for-updates", "true").equals("on-startup", ignoreCase = true)) {
            updateChecker.checkNow()
        }
    }

    fun getPlayerSetting(p: Player): PlayerSetting? {
        if (playerSettings!!.containsKey(p.uniqueId)) {
            return playerSettings!!.get(p.uniqueId)
        }

        val setting: PlayerSetting?
        if (getPlayerFile(p.uniqueId).exists()) {
            setting = PlayerSetting(getPlayerFile(p.uniqueId))
        } else {
            setting = PlayerSetting()
        }

        playerSettings!!.put(p.uniqueId, setting)

        return setting
    }

    fun getPlayerFile(uuid: UUID): File {
        return File(dataFolder.toString() + File.separator + "playerdata" + File.separator + uuid.toString() + ".yml")
    }

    fun reloadCompleteConfig(reload: Boolean) {
        reloadConfig()
        createConfig()
        File(dataFolder.toString() + File.separator + "playerdata").mkdirs()
        if (reload) {
            if (updateChecker != null) {
                updateChecker.stop()
            }
            saveAllPlayerSettings()
        }
        messages = Messages(this)
        initUpdateChecker()
        blockUtils = BlockUtils(this)
        visualizer = de.jeff_media.InvUnload.Visualizer(this)
        val groupsFile = File(this.dataFolder.toString() + File.separator + "groups.yml")
        groupUtils = GroupUtils(this, groupsFile)
        server.pluginManager.registerEvents(PlayerListener(this), this)
        playerSettings = HashMap<UUID?, PlayerSetting?>()
    }

    private fun saveAllPlayerSettings() {
        for (entry in playerSettings!!.entries) {
            entry.value.save(getPlayerFile(entry.key!!), this)
        }
    }

    companion object {
        var instance: Main?
            get() = instance
            private set
    }
}
