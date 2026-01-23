package org.xodium.vanillaplus.managers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.advancement.Advancement
import org.bukkit.entity.Player
import org.xodium.vanillaplus.modules.PlayerModule
import org.xodium.vanillaplus.utils.Utils.MM

/** Manages player messages and internationalization. */
internal object PlayerMessageManager {
    private val config = PlayerModule.config.playerModule.i18n

    /**
     * Handles the player join message.
     * @param player The player who joined.
     * @return The formatted join message component, or null if no message is set.
     */
    fun handleJoin(player: Player): Component? {
        if (config.playerJoinMsg.isEmpty()) return null

        return MM.deserialize(config.playerJoinMsg, Placeholder.component("player", player.displayName()))
    }

    /**
     * Handles the player leave message.
     * @param player The player who left.
     * @return The formatted leave message component, or null if no message is set.
     */
    fun handleQuit(player: Player): Component? {
        if (config.playerQuitMsg.isEmpty()) return null

        return MM.deserialize(config.playerQuitMsg, Placeholder.component("player", player.displayName()))
    }

    /**
     * Handles the player death message.
     * @param player The player who died.
     * @param killer The player who killed them.
     * @return The formatted death message component, or null if no message is set.
     */
    fun handleDeath(
        player: Player,
        killer: Player?,
    ): Component? {
        if (config.playerDeathMsg.isEmpty() || config.playerDeathByPlayerMsg.isEmpty()) return null

        if (killer != null) {
            return MM.deserialize(
                config.playerDeathByPlayerMsg,
                Placeholder.component("player", player.displayName()),
                Placeholder.component("killer", killer.displayName()),
            )
        }

        return MM.deserialize(config.playerDeathMsg, Placeholder.component("player", player.displayName()))
    }

    /**
     * Handles the player death screen message.
     * @return The formatted death screen message component, or null if no message is set.
     */
    fun handleDeathScreen(): Component? {
        if (config.playerDeathScreenMsg.isEmpty()) return null

        return MM.deserialize(config.playerDeathScreenMsg)
    }

    /**
     * Handles the player advancement completion message.
     * @param player The player who completed the advancement.
     * @param advancement The advancement that was completed.
     * @return The formatted advancement completion message component, or null if no message is set.
     */
    fun handleAdvancement(
        player: Player,
        advancement: Advancement,
    ): Component? {
        if (config.playerAdvancementDoneMsg.isEmpty()) return null

        return MM.deserialize(
            config.playerAdvancementDoneMsg,
            Placeholder.component("player", player.displayName()),
            Placeholder.component("advancement", advancement.displayName()),
        )
    }

    /**
     * Handles the player kick message.
     * @param reason The reason for the kick.
     * @return The formatted kick message component, or null if no message is set.
     */
    fun handleKick(reason: Component): Component? {
        if (config.playerKickMsg.isEmpty()) return null

        return MM.deserialize(config.playerKickMsg, Placeholder.component("reason", reason))
    }
}
