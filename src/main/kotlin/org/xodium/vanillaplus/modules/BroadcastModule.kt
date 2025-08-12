package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.roseFmt
import org.xodium.vanillaplus.utils.FmtUtils.spellbiteFmt

/** Represents a module handling broadcasting mechanics within the system. */
internal class BroadcastModule : ModuleInterface<BroadcastModule.Config> {
    override val config: Config = Config()

    init {
        if (enabled()) broadcast()
    }

    private fun broadcast() {
        instance.server.scheduler.runTaskTimer(
            instance,
            Runnable {
                config.broadcastData.forEach { instance.server.broadcast(it.mm()) }
            },
            config.broadcastDelay,
            config.broadcastPeriod,
        )
    }

    data class Config(
        override var enabled: Boolean = true,
        var broadcastData: List<String> =
            listOf(
                "\uD83D\uDCE2 ${"Signs can make use of ".spellbiteFmt()}<click:open_url:'https://docs.advntr.dev/minimessage/format.html'><hover:show_text:'${"Click Me!".fireFmt()}'>${"[<u>MiniMessage</u>]".roseFmt()}${" formatting!".spellbiteFmt()}",
            ),
        var broadcastDelay: Long = 1000L,
        var broadcastPeriod: Long = 1000L,
    ) : ModuleInterface.Config
}
