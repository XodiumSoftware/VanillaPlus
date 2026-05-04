@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus.mechanics.player

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent
import io.papermc.paper.event.player.PlayerServerFullCheckEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.xodium.illyriaplus.interfaces.MechanicInterface
import org.xodium.illyriaplus.managers.PlayerMessageManager

/** Handles player-related messages (join, quit, death, advancements, etc.). */
internal object MessagesMechanic : MechanicInterface {
    /** Player join, quit, death, and kick message strings. */
    object PlayerMessages {
        const val JOIN: String = "<green>➕<reset> <gradient:#FFE259:#FFA751>›</gradient> <player>"
        const val QUIT: String = "<red>➖<reset> <gradient:#FFE259:#FFA751>›</gradient> <player>"
        const val DEATH_BY_PLAYER: String = "<killer> <gradient:#FFE259:#FFA751>⚔</gradient> <player>"
        const val DEATH: String =
            "<gradient:#FFE259:#FFA751>💀</gradient> <gradient:#FFE259:#FFA751>›</gradient> <cause>"
        const val DEATH_SCREEN: String = "☠"
        const val KICK: String =
            "<red>❌<reset> <gradient:#FFE259:#FFA751>›</gradient> <player> " +
                "<gradient:#FFE259:#FFA751>reason:</gradient> <reason>"
        const val SET_SPAWN: String =
            "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> <notification>"
    }

    /** Advancement completion message strings by type (task, goal, challenge). */
    object AdvancementMessages {
        const val TASK: String =
            "🎉 <gradient:#FFE259:#FFA751>›</gradient> <player> " +
                "<gradient:#FFE259:#FFA751>has made the advancement:</gradient> <advancement>"
        const val GOAL: String =
            "🎉 <gradient:#FFE259:#FFA751>›</gradient> <player> " +
                "<gradient:#FFE259:#FFA751>has reached the goal:</gradient> <advancement>"
        const val CHALLENGE: String =
            "🎉 <gradient:#FFE259:#FFA751>›</gradient> <player> " +
                "<gradient:#FFE259:#FFA751>has completed the challenge:</gradient> <advancement>"
    }

    /** Login denial messages (server full, access denied). */
    object LoginMessages {
        const val FULL: String =
            "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> " +
                "The server is full."
        const val DENIED: String =
            "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> " +
                "You are not allowed to join this server."
    }

    /** Bed enter failure messages by reason. */
    object BedEnterMessages {
        const val TOO_FAR_AWAY: String =
            "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> " +
                "You are too far away from the bed."
        const val OBSTRUCTED: String =
            "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> Your bed is obstructed."
        const val NOT_SAFE: String =
            "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> " +
                "You cannot sleep while monsters are nearby."
        const val EXPLOSION: String =
            "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> You cannot sleep here."
        const val OTHER: String = ""
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        event.joinMessage(PlayerMessageManager.handleJoin(event.player) ?: return)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerQuitEvent) {
        event.quitMessage(PlayerMessageManager.handleQuit(event.player) ?: return)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun on(event: PlayerServerFullCheckEvent) {
        if (event.isAllowed) return

        event.deny(PlayerMessageManager.handleServerFull() ?: return)
    }

    @Suppress("UnstableApiUsage")
    @EventHandler(priority = EventPriority.HIGH)
    fun on(event: PlayerConnectionValidateLoginEvent) {
        if (event.isAllowed) return

        event.kickMessage(PlayerMessageManager.handleLoginDenied() ?: return)
    }

    @EventHandler
    fun on(event: PlayerKickEvent) {
        event.leaveMessage(PlayerMessageManager.handleKick(event.reason()) ?: return)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerDeathEvent) {
        val killer = event.entity.killer
        val deathMessage =
            if (killer != null) {
                PlayerMessageManager.handleDeath(event.player, killer)
            } else {
                PlayerMessageManager.handleDeathNoPvp(event.player, event.deathMessage())
            }

        if (deathMessage != null) event.deathMessage(deathMessage)

        event.deathScreenMessageOverride(PlayerMessageManager.handleDeathScreen())
    }

    @EventHandler
    fun on(event: PlayerAdvancementDoneEvent) {
        event.message(PlayerMessageManager.handleAdvancement(event.player, event.advancement) ?: return)
    }

    @EventHandler
    fun on(event: PlayerSetSpawnEvent) {
        event.notification = PlayerMessageManager.handleSetSpawn(event.notification ?: return) ?: return
    }

    @Suppress("UnstableApiUsage")
    @EventHandler
    fun on(event: PlayerBedEnterEvent) {
        event.player.sendMessage(PlayerMessageManager.handleBedEnter(event.enterAction().problem() ?: return) ?: return)
    }
}
