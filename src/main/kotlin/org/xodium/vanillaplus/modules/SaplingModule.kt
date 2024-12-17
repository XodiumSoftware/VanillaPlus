package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface

class SaplingModule : ModuleInterface {
    private val cn: String = javaClass.simpleName

    override fun enabled(): Boolean {
        return instance.config.getBoolean("$cn.enable")
    }
}
