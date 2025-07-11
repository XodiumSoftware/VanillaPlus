package org.xodium.vanillaplus.modules

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scoreboard.Scoreboard
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm


class NameTagModule : ModuleInterface<NameTagModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean {
        if (!config.enabled) return false

        val protocollib = instance.server.pluginManager.getPlugin("ProtocolLib") != null
        if (!protocollib) instance.logger.warning("ProtocolLib not found, disabling NameTagModule")

        return protocollib
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return
        val player = event.player
        player.nametag("test ", " test")
        player.nametag()
    }

    /** Gets the nametag for a player. */
    private fun Player.nametag(): Scoreboard = this.scoreboard

    /**
     * Sets the nametag for a player with a prefix and suffix.
     * @param prefix The prefix to be added to the nametag.
     * @param suffix The suffix to be added to the nametag.
     */
    private fun Player.nametag(prefix: String, suffix: String) {
        instance.server.scoreboardManager.mainScoreboard.let { scoreboard ->
            (scoreboard.getTeam(this.name) ?: scoreboard.registerNewTeam(this.name)).apply {
                prefix(prefix.mm())
                suffix(suffix.mm())
                addEntry(this.name)
            }
            this.scoreboard = scoreboard
        }
    }

    data class Config(
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}