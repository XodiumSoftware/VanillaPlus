package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface

class SaplingModule : ModuleInterface {
    private val cn: String = javaClass.getSimpleName()
    override fun enabled(): Boolean {
        return instance.config.getBoolean(cn + ModuleInterface.CONFIG.ENABLE)
    }

    override fun config() {
        instance.config.addDefault(cn + ModuleInterface.CONFIG.ENABLE, true)
        instance.saveConfig()
    }
}
