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
     * @param killer The player who killed them, if applicable.
     * @return A pair containing the formatted death message component and death screen message component,
     * or nulls if no messages are set.
     */
    fun handleDeath(
        player: Player,
        killer: Player? = null,
    ): Component? {
        if (config.playerDeathMsg.isEmpty()) return null

        return MM.deserialize(
            config.playerDeathMsg,
            Placeholder.component("player", player.displayName()),
            Placeholder.component("killer", killer?.displayName() ?: MM.deserialize("Unknown")),
        )
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
}
