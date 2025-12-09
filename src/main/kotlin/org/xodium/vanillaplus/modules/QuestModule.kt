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
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import org.xodium.vanillaplus.utils.ExtUtils.mm
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.*

/** Represents a module handling quest mechanics within the system. */
internal object QuestModule : ModuleInterface {
    private val activeQuests = mutableMapOf<UUID, MutableSet<UUID>>()
    private val assignedQuests = mutableMapOf<UUID, MutableSet<UUID>>()
    private val questResetTimes = mutableMapOf<UUID, Long>()
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

        if (activeQuests[player.uniqueId]?.contains(questUuid) == true) {
            player.sendMessage("<red>You already have this quest active!".mm())
            return
        }

        acceptQuest(player, questUuid)
    }

    /**
     * Handles player join events by initializing their active quests.
     * @param event The PlayerJoinEvent to handle.
     */
    private fun handlePlayerJoin(event: PlayerJoinEvent) {
        val playerUuid = event.player.uniqueId

        activeQuests.putIfAbsent(playerUuid, mutableSetOf())

        if (!assignedQuests.containsKey(playerUuid) || !questResetTimes.containsKey(playerUuid)) {
            assignQuestsToPlayer(playerUuid)
            event.player.sendMessage("<green>New weekly quests have been assigned!".mm())
        }
    }

    /**
     * Accepts a quest for the player.
     * @param player The player accepting the quest.
     * @param questUuid The UUID of the quest being accepted.
     */
    private fun acceptQuest(
        player: Player,
        questUuid: UUID,
    ) {
        activeQuests.getOrPut(player.uniqueId) { mutableSetOf() }.add(questUuid)
        player.sendMessage("<green>Quest accepted! Check your progress with /quests".mm())
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
        if (activeQuests[player.uniqueId]?.remove(questUuid) != true) return

        player.inventory.addItem(QuestInventory.crystal(crystalAmount))
        player.sendMessage("<green>Quest completed! You earned $crystalAmount crystals!".mm())
    }

    /** Checks if it's time for weekly reset and resets quests for all players. */
    private fun checkWeeklyReset() {
        val currentTime = System.currentTimeMillis()

        questResetTimes.forEach { (playerUuid, resetTime) ->
            if (currentTime >= resetTime) {
                activeQuests[playerUuid]?.clear()
                assignQuestsToPlayer(playerUuid)

                val player = instance.server.getPlayer(playerUuid)
                player?.sendMessage("<yellow>Your weekly quests have been reset!".mm())
            }
        }
    }

    /**
     * Assigns new quests to a player.
     * @param playerUuid The UUID of the player.
     */
    private fun assignQuestsToPlayer(playerUuid: UUID) {
        val newQuests = QuestInventory.generateWeeklyQuests()
        assignedQuests[playerUuid] = newQuests.toMutableSet()
        questResetTimes[playerUuid] = getNextResetTime()
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
