@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.util.*

/** Provides access to player-specific quest data including assigned quests, active quests, and reset times. */
internal object QuestPDC {
    private val ASSIGNED_QUESTS_KEY = NamespacedKey(instance, "assigned_quests")
    private val ACTIVE_QUESTS_KEY = NamespacedKey(instance, "active_quests")
    private val QUEST_RESET_TIME_KEY = NamespacedKey(instance, "quest_reset_time")

    /**
     * Gets or sets the player's assigned quest UUIDs.
     * @return A set of UUIDs representing the player's assigned quests.
     */
    var Player.assignedQuests: Set<UUID>
        get() {
            val data = persistentDataContainer.get(ASSIGNED_QUESTS_KEY, PersistentDataType.STRING) ?: return emptySet()
            return data
                .split(",")
                .filter { it.isNotBlank() }
                .map { UUID.fromString(it) }
                .toSet()
        }
        set(value) {
            if (value.isEmpty()) {
                persistentDataContainer.remove(ASSIGNED_QUESTS_KEY)
            } else {
                persistentDataContainer.set(ASSIGNED_QUESTS_KEY, PersistentDataType.STRING, value.joinToString(","))
            }
        }

    /**
     * Gets or sets the player's active quest UUIDs.
     * @return A set of UUIDs representing the player's active quests.
     */
    var Player.activeQuests: Set<UUID>
        get() {
            val data = persistentDataContainer.get(ACTIVE_QUESTS_KEY, PersistentDataType.STRING) ?: return emptySet()
            return data
                .split(",")
                .filter { it.isNotBlank() }
                .map { UUID.fromString(it) }
                .toSet()
        }
        set(value) {
            if (value.isEmpty()) {
                persistentDataContainer.remove(ACTIVE_QUESTS_KEY)
            } else {
                persistentDataContainer.set(ACTIVE_QUESTS_KEY, PersistentDataType.STRING, value.joinToString(","))
            }
        }

    /**
     * Gets or sets the player's quest reset time in milliseconds.
     * @return The quest reset time as a Long, or null if not set.
     */
    var Player.questResetTime: Long?
        get() = persistentDataContainer.get(QUEST_RESET_TIME_KEY, PersistentDataType.LONG)
        set(value) {
            if (value == null) {
                persistentDataContainer.remove(QUEST_RESET_TIME_KEY)
            } else {
                persistentDataContainer.set(QUEST_RESET_TIME_KEY, PersistentDataType.LONG, value)
            }
        }

    /**
     * Adds a quest UUID to the player's active quests.
     * @param questUuid The UUID of the quest to add.
     */
    fun Player.addActiveQuest(questUuid: UUID) {
        activeQuests = activeQuests + questUuid
    }

    /**
     * Removes a quest UUID from the player's active quests.
     * @param questUuid The UUID of the quest to remove.
     * @return True if the quest was present and removed, false otherwise.
     */
    fun Player.removeActiveQuest(questUuid: UUID): Boolean {
        val current = activeQuests
        val wasPresent = current.contains(questUuid)
        activeQuests = current - questUuid
        return wasPresent
    }
}
