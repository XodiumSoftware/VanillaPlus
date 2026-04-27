package org.xodium.vanillaplus.managers

import io.papermc.paper.advancement.AdvancementDisplay
import io.papermc.paper.block.bed.BedEnterProblem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.advancement.Advancement
import org.bukkit.entity.Player
import org.xodium.vanillaplus.modules.PlayerMechanic
import org.xodium.vanillaplus.utils.Utils.MM

/** Manages player messages and internationalization. */
internal object PlayerMessageManager {
    /**
     * Handles the player join message.
     * @param player The player who joined.
     * @return The formatted join message component, or null if no message is set.
     */
    fun handleJoin(player: Player): Component? =
        MM.deserialize(
            PlayerMechanic.Config.PlayerMessages.JOIN
                .takeIf { it.isNotEmpty() } ?: return null,
            Placeholder.component("player", player.displayName()),
        )

    /**
     * Handles the player leave message.
     * @param player The player who left.
     * @return The formatted leave message component, or null if no message is set.
     */
    fun handleQuit(player: Player): Component? =
        MM.deserialize(
            PlayerMechanic.Config.PlayerMessages.QUIT
                .takeIf { it.isNotEmpty() } ?: return null,
            Placeholder.component("player", player.displayName()),
        )

    /**
     * Handles the player death message.
     * @param player The player who died.
     * @param killer The player who killed them.
     * @return The formatted death message component, or null if no message is set.
     */
    fun handleDeath(
        player: Player,
        killer: Player?,
    ): Component? =
        MM.deserialize(
            PlayerMechanic.Config.PlayerMessages.DEATH_BY_PLAYER
                .takeIf { it.isNotEmpty() } ?: return null,
            Placeholder.component("player", player.displayName()),
            Placeholder.component("killer", (killer ?: return null).displayName()),
        )

    /**
     * Handles the non-PvP death message (fall, fire, drowning, etc.).
     * @param player The player who died.
     * @param cause The vanilla death message component.
     * @return The formatted death message component, or null if no message is set.
     */
    fun handleDeathNoPvp(
        player: Player,
        cause: Component?,
    ): Component? =
        MM.deserialize(
            PlayerMechanic.Config.PlayerMessages.DEATH
                .takeIf { it.isNotEmpty() } ?: return null,
            Placeholder.component("player", player.displayName()),
            Placeholder.component("cause", cause ?: return null),
        )

    /**
     * Handles the player death screen message.
     * @return The formatted death screen message component, or null if no message is set.
     */
    fun handleDeathScreen(): Component? =
        MM.deserialize(
            PlayerMechanic.Config.PlayerMessages.DEATH_SCREEN
                .takeIf { it.isNotEmpty() } ?: return null,
        )

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
                AdvancementDisplay.Frame.TASK -> PlayerMechanic.Config.AdvancementMessages.TASK
                AdvancementDisplay.Frame.GOAL -> PlayerMechanic.Config.AdvancementMessages.GOAL
                AdvancementDisplay.Frame.CHALLENGE -> PlayerMechanic.Config.AdvancementMessages.CHALLENGE
            }.takeIf { it.isNotEmpty() } ?: return null,
            Placeholder.component("player", player.displayName()),
            Placeholder.component("advancement", display.title()),
        )
    }

    /**
     * Handles the kick message shown when the server is full.
     * @return The formatted kick message component, or null if no message is set.
     */
    fun handleServerFull(): Component? =
        MM.deserialize(
            PlayerMechanic.Config.LoginMessages.FULL
                .takeIf { it.isNotEmpty() } ?: return null,
        )

    /**
     * Handles the kick message shown when a player is denied login (ban, IP ban, whitelist).
     * @return The formatted kick message component, or null if no message is set.
     */
    fun handleLoginDenied(): Component? =
        MM.deserialize(
            PlayerMechanic.Config.LoginMessages.DENIED
                .takeIf { it.isNotEmpty() } ?: return null,
        )

    /**
     * Handles the player kick message.
     * @param reason The reason for the kick.
     * @return The formatted kick message component, or null if no message is set.
     */
    fun handleKick(reason: Component): Component? =
        MM.deserialize(
            PlayerMechanic.Config.PlayerMessages.KICK
                .takeIf { it.isNotEmpty() } ?: return null,
            Placeholder.component("reason", reason),
        )

    /**
     * Handles the bed enter failure message.
     * @param problem The problem preventing the player from sleeping.
     * @return The formatted bed enter message component, or null if no message is set for this problem.
     */
    @Suppress("UnstableApiUsage")
    fun handleBedEnter(problem: BedEnterProblem): Component? =
        MM.deserialize(
            when (problem) {
                BedEnterProblem.TOO_FAR_AWAY -> PlayerMechanic.Config.BedEnterMessages.TOO_FAR_AWAY
                BedEnterProblem.OBSTRUCTED -> PlayerMechanic.Config.BedEnterMessages.OBSTRUCTED
                BedEnterProblem.NOT_SAFE -> PlayerMechanic.Config.BedEnterMessages.NOT_SAFE
                BedEnterProblem.EXPLOSION -> PlayerMechanic.Config.BedEnterMessages.EXPLOSION
                else -> PlayerMechanic.Config.BedEnterMessages.OTHER
            }.takeIf { it.isNotEmpty() } ?: return null,
        )

    /**
     * Handles the player set spawn notification.
     * @param notification The original notification component.
     * @return The formatted set spawn notification component.
     */
    fun handleSetSpawn(notification: Component): Component? =
        MM.deserialize(
            PlayerMechanic.Config.PlayerMessages.SET_SPAWN
                .takeIf { it.isNotEmpty() } ?: return null,
            Placeholder.component("notification", notification),
        )
}
