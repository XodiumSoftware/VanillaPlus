package org.xodium.vanillaplus.modules

import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling quest mechanics within the system. */
class QuestModule : ModuleInterface<QuestModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    init {
        if (enabled()) UltimateAdvancementAPI.getInstance(instance)
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}