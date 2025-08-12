package org.xodium.vanillaplus.modules

import io.papermc.paper.event.player.PlayerOpenSignEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.SignChangeEvent
import org.xodium.vanillaplus.data.PlayerData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.pt
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.FmtUtils.roseFmt
import org.xodium.vanillaplus.utils.FmtUtils.spellbiteFmt

/** Represents a module handling sign mechanics within the system. */
internal class SignModule : ModuleInterface<SignModule.Config> {
    override val config: Config = Config()

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: SignChangeEvent) {
        if (!enabled()) return

        val lines = event.lines()
        for (i in lines.indices) {
            if (containsMiniMessageTags(lines[i])) event.line(i, lines[i].pt().mm())
        }
    }

    @EventHandler
    fun on(event: PlayerOpenSignEvent) {
        val player = event.player
        if (!enabled() || PlayerData.get(player).signTutorial) return
        event.isCancelled = true
        player.showTitle(Title.title(config.title.mm(), config.subtitle.mm()))
        player.sendMessage(config.tutorialText.mm())
//        PlayerData.update(player, PlayerData.get(player).copy(signTutorial = true))
    }

    /**
     * Determines if the given component's plaintext representation contains MiniMessage tags.
     * @param component the component to inspect for MiniMessage tags in its plaintext form.
     * @return true if MiniMessage tags are found, false otherwise.
     */
    private fun containsMiniMessageTags(component: Component): Boolean = config.miniMessageRegex.toRegex().containsMatchIn(component.pt())

    data class Config(
        override var enabled: Boolean = true,
        var miniMessageRegex: String = "</?[a-zA-Z0-9_#:-]+.*?>",
        var title: String = "Sign Tutorial".fireFmt(),
        var subtitle: String = "Read the chat...".mangoFmt(),
        var tutorialText: String =
            "${
                "⯈".mangoFmt(
                    true,
                )
            } ${"Signs can make use of ".spellbiteFmt()}<click:open_url:'https://docs.advntr.dev/minimessage/format.html'><hover:show_text:'${"Click Me!".fireFmt()}'>${"[<u>MiniMessage</u>]".roseFmt()}${" formatting!".spellbiteFmt()}",
    ) : ModuleInterface.Config
}
