package org.xodium.vanillaplus

import org.bukkit.plugin.java.JavaPlugin
import org.xodium.vanillaplus.commands.ReloadCommand
import java.util.*

class VanillaPlus : JavaPlugin() {
    override fun onEnable() {
        if (!isPaper) {
            disablePlugin(IS_PAPER_MSG)
            return
        }
        if (!isSupportedVersion) {
            disablePlugin(IS_SUPPORTED_VERSION_MSG)
            return
        }
        saveDefaultConfig()
        ReloadCommand
        ModuleManager
    }

    private fun disablePlugin(msg: String?) {
        logger.severe(msg)
        server.pluginManager.disablePlugin(this)
    }

    private val isSupportedVersion: Boolean
        get() = Arrays.stream<String?>(V)
            .anyMatch { v: String? -> server.version.contains(v.toString()) }

    private val isPaper: Boolean
        get() = Arrays.stream<String?>(PAPER)
            .anyMatch { v: String? -> server.name.contains(v.toString()) }

    companion object {
        private val V = arrayOf<String?>("1.21.3")
        private val PAPER = arrayOf<String?>("Paper")
        private const val IS_PAPER_MSG = "This plugin is not compatible with non-Paper servers."
        private val IS_SUPPORTED_VERSION_MSG =
            "This plugin requires Paper version(s): ${V.joinToString(", ")}"

        const val PREFIX: String = "<gold>[<dark_aqua>VanillaPlus<gold>] <reset>"

        @JvmStatic
        val instance: VanillaPlus
            get() = getPlugin<VanillaPlus>(VanillaPlus::class.java)
    }
}