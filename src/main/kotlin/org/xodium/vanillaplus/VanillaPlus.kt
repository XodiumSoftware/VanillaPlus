package org.xodium.vanillaplus

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain
import com.fren_gor.ultimateAdvancementAPI.database.impl.SQLite
import org.bukkit.plugin.java.JavaPlugin
import org.xodium.vanillaplus.managers.ModuleManager
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import java.io.File


/** Main class of the plugin. */
class VanillaPlus : JavaPlugin() {

    private lateinit var advancementMain: AdvancementMain

    companion object {
        private const val SUPPORTED_VERSION = "1.21.7"
        private const val SUPPORTED_PLATFORM = "Paper"
        private const val UNSUPPORTED_PLATFORM_MSG =
            "This plugin requires a supported server platform. Supported platforms: ${SUPPORTED_PLATFORM}."
        private const val UNSUPPORTED_VERSION_MSG =
            "This plugin requires a supported server version. Supported versions: ${SUPPORTED_VERSION}."

        val PREFIX: String =
            "${"[".mangoFmt(true)}${VanillaPlus::class.simpleName.toString().fireFmt()}${"]".mangoFmt()}"

        @JvmStatic
        val instance: VanillaPlus by lazy { getPlugin(VanillaPlus::class.java) }
    }

    override fun onLoad() {
        advancementMain = AdvancementMain(this)
        advancementMain.load()
    }

    override fun onEnable() {
        advancementMain.enable { SQLite(advancementMain, File(dataFolder, "advancements.db")) }
        when {
            !server.version.contains(SUPPORTED_VERSION) -> disablePlugin(UNSUPPORTED_VERSION_MSG)
            !server.name.contains(SUPPORTED_PLATFORM) -> disablePlugin(UNSUPPORTED_PLATFORM_MSG)
            else -> ModuleManager
        }
    }

    override fun onDisable() = advancementMain.disable()

    /**
     * Disable the plugin and log the message.
     * @param msg The message to log.
     */
    private fun disablePlugin(msg: String) {
        logger.severe(msg)
        server.pluginManager.disablePlugin(this)
    }
}
