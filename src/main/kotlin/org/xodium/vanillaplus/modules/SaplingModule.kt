package org.xodium.vanillaplus.modules

import org.bukkit.configuration.file.FileConfiguration
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface

class SaplingModule : ModuleInterface {
    private val cn: String = javaClass.getSimpleName()
    override fun enabled(): Boolean {
        return FC.getBoolean(cn + ModuleInterface.CONFIG.ENABLE)
    }

    override fun config() {
        FC.addDefault(cn + ModuleInterface.CONFIG.ENABLE, true)
        VP.saveConfig()
    }

    companion object {
        private val VP = instance
        private val FC: FileConfiguration = VP.config
    }
}
