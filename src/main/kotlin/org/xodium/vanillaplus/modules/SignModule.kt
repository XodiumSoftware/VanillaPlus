package org.xodium.vanillaplus.modules

import io.papermc.paper.event.player.PlayerOpenSignEvent
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.pt
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.roseFmt
import org.xodium.vanillaplus.utils.FmtUtils.spellbiteFmt

/** Represents a module handling sign mechanics within the system. */
internal class SignModule : ModuleInterface<SignModule.Config> {
    override val config: Config = Config()

    private val scoreboardID = "${instance::class.simpleName}_${this::class.simpleName}_scoreboard".lowercase()

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
        if (!enabled()) return
        event.player.scoreboard = scoreboard()
    }

    /**
     * Determines if the given component's plaintext representation contains MiniMessage tags.
     * @param component the component to inspect for MiniMessage tags in its plaintext form.
     * @return true if MiniMessage tags are found, false otherwise.
     */
    private fun containsMiniMessageTags(component: Component): Boolean = config.miniMessageRegex.toRegex().containsMatchIn(component.pt())

    /**
     * Creates and returns a sidebar scoreboard.
     * @return a newly created [Scoreboard] instance with the sidebar display slot set.
     */
    private fun scoreboard(): Scoreboard =
        instance.server.scoreboardManager.newScoreboard.apply {
            registerNewObjective(scoreboardID, Criteria.DUMMY, config.l18n.scoreboardTitle.mm()).apply {
                displaySlot = DisplaySlot.SIDEBAR
            }
        }

    data class Config(
        override var enabled: Boolean = true,
        var miniMessageRegex: String = "</?[a-zA-Z0-9_#:-]+.*?>",
        var tutorialText: List<String> =
            listOf(
                "\uD83D\uDCE2 ${"Signs can make use of ".spellbiteFmt()}<click:open_url:'https://docs.advntr.dev/minimessage/format.html'><hover:show_text:'${"Click Me!".fireFmt()}'>${"[<u>MiniMessage</u>]".roseFmt()}${" formatting!".spellbiteFmt()}",
            ),
        var l18n: L18n = L18n(),
    ) : ModuleInterface.Config {
        data class L18n(
            var scoreboardTitle: String = "Tutorial".fireFmt(),
        )
    }
}
