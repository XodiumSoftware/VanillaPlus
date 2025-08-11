package org.xodium.vanillaplus.modules

import de.oliver.fancyholograms.api.data.TextHologramData
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.SignChangeEvent
import org.xodium.vanillaplus.hooks.FancyHologramsHook
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.pt

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

    /**
     * Determines if the given component's plaintext representation contains MiniMessage tags.
     * @param component the component to inspect for MiniMessage tags in its plaintext form
     * @return true if MiniMessage tags are found, false otherwise
     */
    private fun containsMiniMessageTags(component: Component): Boolean = config.miniMessageRegex.toRegex().containsMatchIn(component.pt())

    private fun hologram(player: Player) {
        val hook = FancyHologramsHook
        if (!hook.enabled()) return

        val data = TextHologramData("Tutorial", player.location)
        val hologram = hook.manager.create(data)
        hook.manager.addHologram(hologram)
    }

    data class Config(
        override var enabled: Boolean = true,
        var miniMessageRegex: String = "</?[a-zA-Z0-9_#:-]+.*?>",
    ) : ModuleInterface.Config
}
