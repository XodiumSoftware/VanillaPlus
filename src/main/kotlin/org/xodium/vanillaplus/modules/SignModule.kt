package org.xodium.vanillaplus.modules

import io.papermc.paper.event.player.PlayerOpenSignEvent
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.SignChangeEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.pt
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt

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

    // TODO: save tutorial status in [PlayerData]

    @EventHandler
    fun on(event: PlayerOpenSignEvent) {
        if (!enabled()) return
        event.isCancelled = true
    }

    /**
     * Determines if the given component's plaintext representation contains MiniMessage tags.
     * @param component the component to inspect for MiniMessage tags in its plaintext form
     * @return true if MiniMessage tags are found, false otherwise
     */
    private fun containsMiniMessageTags(component: Component): Boolean = config.miniMessageRegex.toRegex().containsMatchIn(component.pt())

    data class Config(
        override var enabled: Boolean = true,
        var miniMessageRegex: String = "</?[a-zA-Z0-9_#:-]+.*?>",
        var tutorial: List<String> =
            listOf(
                "Sign Formatting Tutorial".fireFmt(),
                "Signs can make use of [MiniMessage] formatting",
                "For example:",
                "  <red>This text is red",
                "  <#ff0000>This text is red in RGB",
                "  <bold>This text is bold",
                "  <italic>This text is italic",
                "  <underlined>This text is underlined",
                "  <strikethrough>This text is strikethrough",
                "  <obfuscated>This text is obfuscated",
                "  <hover:show_text:'Hover text'>This text has a hover tooltip",
                "  <click:run_command:'/help'>This text has a click action",
                "",
                "<Click to Close>",
            ),
    ) : ModuleInterface.Config
}
