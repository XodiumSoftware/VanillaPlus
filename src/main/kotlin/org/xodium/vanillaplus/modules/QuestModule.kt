@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.inventories.QuestInventory
import org.xodium.vanillaplus.pdcs.QuestPDC.activeQuests
import org.xodium.vanillaplus.pdcs.QuestPDC.assignedQuests
import org.xodium.vanillaplus.pdcs.QuestPDC.questResetTime
import org.xodium.vanillaplus.pdcs.QuestPDC.removeActiveQuest
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.prefix
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.*

/** Represents a module handling quest mechanics within the system. */
internal object QuestModule : ModuleInterface {
    private val questUuidKey = NamespacedKey(instance, "quest_uuid")

    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("quests")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .playerExecuted { player, _ -> handleQuests(player) },
                "Opens the quest inventory",
                listOf("q"),
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.quests".lowercase(),
                "Allows use of the quests command",
                PermissionDefault.TRUE,
            ),
        )

    init {
        instance.server.scheduler.runTaskTimer(
            instance,
            Runnable { checkWeeklyReset() },
            0L,
            1200L,
        )
    }

    @EventHandler
    fun on(event: InventoryClickEvent) = handleInventoryClick(event)

    @EventHandler
    fun on(event: PlayerJoinEvent) = handlePlayerJoin(event)

    /**
     * Handles the quest command by opening the quest inventory for the player.
     * @param player The player who executed the command.
     */
    private fun handleQuests(player: Player) {
        player.openInventory(QuestInventory.inventory)
    }

    /**
     * Checks and resets weekly quests for all players if the reset time has passed.
     * @param event The InventoryClickEvent to handle.
     */
    private fun handleInventoryClick(event: InventoryClickEvent) {
        if (event.inventory.holder != QuestInventory) return
        event.isCancelled = true

        val player = event.whoClicked as? Player ?: return
        val item = event.currentItem ?: return
        val questUuid =
            item.itemMeta
                ?.persistentDataContainer
                ?.get(questUuidKey, PersistentDataType.STRING)
                ?.let { UUID.fromString(it) } ?: return

        if (!player.assignedQuests.contains(questUuid)) {
            player.sendMessage("${instance.prefix} <red>This quest is not available for you!".mm())
            return
        }

        if (!player.activeQuests.contains(questUuid)) {
            player.sendMessage("${instance.prefix} <red>You haven't completed this quest yet!".mm())
            return
        }

        val quest = QuestInventory.getQuestByUuid(questUuid) ?: return
        completeQuest(player, questUuid, quest.crystals)
    }

    /**
     * Handles player join events by initializing their active quests.
     * @param event The PlayerJoinEvent to handle.
     */
    private fun handlePlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        if (player.assignedQuests.isEmpty() || player.questResetTime == null) {
            assignQuestsToPlayer(player)
            player.sendMessage("${instance.prefix} <green>New weekly quests have been assigned!".mm())
        }
    }

    /**
     * Completes a quest for the player and awards crystals.
     * @param player The player completing the quest.
     * @param questUuid The UUID of the quest being completed.
     * @param crystalAmount The amount of crystals to award.
     */
    fun completeQuest(
        player: Player,
        questUuid: UUID,
        crystalAmount: Int,
    ) {
        if (!player.removeActiveQuest(questUuid)) return

        player.inventory.addItem(QuestInventory.crystal(crystalAmount))
        player.sendMessage("${instance.prefix} <green>Quest completed! You earned $crystalAmount crystals!".mm())
    }

    /** Checks if it's time for weekly reset and resets quests for all players. */
    private fun checkWeeklyReset() {
        val currentTime = System.currentTimeMillis()

        instance.server.onlinePlayers.forEach { player ->
            val resetTime = player.questResetTime ?: return@forEach

            if (currentTime >= resetTime) {
                player.activeQuests = emptySet()
                assignQuestsToPlayer(player)
                player.sendMessage("${instance.prefix} <yellow>Your weekly quests have been reset!".mm())
            }
        }
    }

    /**
     * Assigns new quests to a player.
     * @param playerUuid The UUID of the player.
     */
    private fun assignQuestsToPlayer(player: Player) {
        val newQuests = QuestInventory.generateWeeklyQuests()
        player.assignedQuests = newQuests
        player.activeQuests = newQuests
        player.questResetTime = getNextResetTime()
    }

    /**
     * Calculates the next Sunday at 00:00.
     * @return The timestamp in milliseconds for the next Sunday at 00:00.
     */
    private fun getNextResetTime(): Long {
        val now = LocalDateTime.now(ZoneId.systemDefault())
        val nextSunday =
            now
                .with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
        return nextSunday.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
