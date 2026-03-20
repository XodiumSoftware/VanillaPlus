package org.xodium.vanillaplus.managers

import io.papermc.paper.advancement.AdvancementDisplay
import io.papermc.paper.block.bed.BedEnterProblem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.advancement.Advancement
import org.bukkit.entity.Player
import org.xodium.vanillaplus.modules.PlayerModule
import org.xodium.vanillaplus.utils.Utils.MM

/** Manages player messages and internationalization. */
internal object PlayerMessageManager {
    private val config = PlayerModule.config.i18n

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
        if (config.playerDeathByPlayerMsg.isEmpty() || killer == null) return null

        return MM.deserialize(
            config.playerDeathByPlayerMsg,
            Placeholder.component("player", player.displayName()),
            Placeholder.component("killer", killer.displayName()),
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
     * Handles the player advancement completion message, with different formats per advancement type.
     * @param player The player who completed the advancement.
     * @param advancement The advancement that was completed.
     * @return The formatted advancement completion message component, or null if no message is set or the advancement has no display.
     */
    fun handleAdvancement(
        player: Player,
        advancement: Advancement,
    ): Component? {
        val display = advancement.display ?: return null

        return MM.deserialize(
            when (display.frame()) {
                AdvancementDisplay.Frame.TASK -> config.advancementMessages.task
                AdvancementDisplay.Frame.GOAL -> config.advancementMessages.goal
                AdvancementDisplay.Frame.CHALLENGE -> config.advancementMessages.challenge
            }.takeIf { it.isNotEmpty() } ?: return null,
            Placeholder.component("player", player.displayName()),
            Placeholder.component("advancement", display.title()),
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

    /**
     * Handles the bed enter failure message.
     * @param problem The problem preventing the player from sleeping.
     * @return The formatted bed enter message component, or null if no message is set for this problem.
     */
    @Suppress("UnstableApiUsage")
    fun handleBedEnter(problem: BedEnterProblem): Component? {
        return MM.deserialize(
            when (problem) {
                BedEnterProblem.TOO_FAR_AWAY -> config.bedEnterMessages.tooFarAway
                BedEnterProblem.OBSTRUCTED -> config.bedEnterMessages.obstructed
                BedEnterProblem.NOT_SAFE -> config.bedEnterMessages.notSafe
                BedEnterProblem.EXPLOSION -> config.bedEnterMessages.explosion
                else -> config.bedEnterMessages.other
            }.takeIf { it.isNotEmpty() } ?: return null,
        )
    }

    /**
     * Handles the player set spawn notification.
     * @param notification The original notification component.
     * @return The formatted set spawn notification component.
     */
    fun handleSetSpawn(notification: Component): Component? {
        if (config.playerSetSpawnMsg.isEmpty()) return null

        return MM.deserialize(config.playerSetSpawnMsg, Placeholder.component("notification", notification))
    }
}
