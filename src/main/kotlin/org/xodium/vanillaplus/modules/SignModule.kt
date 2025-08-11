package org.xodium.vanillaplus.modules

import eu.decentsoftware.holograms.api.DHAPI
import eu.decentsoftware.holograms.event.HologramClickEvent
import io.papermc.paper.event.player.PlayerOpenSignEvent
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.SignChangeEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.hooks.DecentHologramsHook
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.pt

/** Represents a module handling sign mechanics within the system. */
internal class SignModule : ModuleInterface<SignModule.Config> {
    override val config: Config = Config()

    private val hologramID = "${instance::class.simpleName}_${this::class.simpleName}_tutorial"

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
        if (!enabled() || !DecentHologramsHook.enabled()) return
        event.isCancelled = true
        hologram(event.player)
    }

    @EventHandler
    fun on(event: HologramClickEvent) {
        if (!enabled() || !DecentHologramsHook.enabled()) return
        DHAPI.removeHologram(event.hologram.name)
    }

    /**
     * Determines if the given component's plaintext representation contains MiniMessage tags.
     * @param component the component to inspect for MiniMessage tags in its plaintext form
     * @return true if MiniMessage tags are found, false otherwise
     */
    private fun containsMiniMessageTags(component: Component): Boolean = config.miniMessageRegex.toRegex().containsMatchIn(component.pt())

    private fun hologram(player: Player) {
        if (!DecentHologramsHook.enabled()) return
        DHAPI.createHologram(hologramID, player.location, true, config.hologramText).apply {
            isDefaultVisibleState = false
            setShowPlayer(player)
        }
    }

    data class Config(
        override var enabled: Boolean = true,
        var miniMessageRegex: String = "</?[a-zA-Z0-9_#:-]+.*?>",
        var hologramText: List<String> = listOf("Tutorial Text"),
    ) : ModuleInterface.Config
}
