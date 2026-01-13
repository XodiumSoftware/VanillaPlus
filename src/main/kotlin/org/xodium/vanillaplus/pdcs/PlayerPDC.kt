package org.xodium.vanillaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.pdcs.PlayerPDC.NICKNAME_KEY
import org.xodium.vanillaplus.pdcs.PlayerPDC.QUESTS_KEY
import org.xodium.vanillaplus.pdcs.PlayerPDC.SCOREBOARD_VISIBILITY_KEY

/**
 * Provides access to player-specific persistent data including nicknames and scoreboard preferences.
 * @property NICKNAME_KEY The namespaced key used for storing nickname data.
 * @property SCOREBOARD_VISIBILITY_KEY The namespaced key used for storing scoreboard visibility preferences.
 * @property QUESTS_KEY The namespaced key used for storing quest data.
 */
internal object PlayerPDC {
    private val NICKNAME_KEY = NamespacedKey(instance, "nickname")
    private val SCOREBOARD_VISIBILITY_KEY = NamespacedKey(instance, "scoreboard_visibility")
    private val QUESTS_KEY = NamespacedKey(instance, "quests")

    private const val QUEST_KEY_PREFIX = "quests_"
    private const val QUEST_VALUE_SEPARATOR = "|"

    /**
     * Data class representing a player's quest data stored in their persistent data container.
     * @property playerUuid The UUID of the player.
     * @property questId The ID of the quest.
     * @property questProgress The progress made in the quest.
     */
    data class QuestPDC(
        val playerUuid: String,
        val questId: Int,
        val questProgress: Int,
    )

    /**
     * Gets or sets the player's nickname in their persistent data container.
     * @receiver The player whose nickname to access.
     * @return The player's nickname, or their actual name if no nickname is set.
     */
    var Player.nickname: String
        get() = persistentDataContainer.get(NICKNAME_KEY, PersistentDataType.STRING) ?: name
        set(value) {
            if (value.isBlank()) {
                persistentDataContainer.remove(NICKNAME_KEY)
            } else {
                persistentDataContainer.set(NICKNAME_KEY, PersistentDataType.STRING, value)
            }
        }

    /**
     * Gets or sets the player's scoreboard visibility preference in their persistent data container.
     * @receiver The player whose scoreboard visibility to access.
     * @return True if the scoreboard is visible, false otherwise.
     */
    var Player.scoreboardVisibility: Boolean
        get() = persistentDataContainer.get(SCOREBOARD_VISIBILITY_KEY, PersistentDataType.BOOLEAN) ?: false
        set(value) = persistentDataContainer.set(SCOREBOARD_VISIBILITY_KEY, PersistentDataType.BOOLEAN, value)

    /**
     * Gets or sets the player's quests in their persistent data container.
     * @receiver The player whose quests to access.
     * @return A string representing the player's quests, or null if not set.
     */
    var Player.quests: List<QuestPDC>?
        get() {
            val uuid = uniqueId.toString()
            val pdc = persistentDataContainer
            val keys =
                pdc.keys
                    .asSequence()
                    .filter { it.namespace == instance.name.lowercase() }
                    .filter { it.key.startsWith(QUEST_KEY_PREFIX) }
                    .toList()

            return keys.mapNotNull { key ->
                val raw = pdc.get(key, PersistentDataType.STRING) ?: return@mapNotNull null
                val parts = raw.split(QUEST_VALUE_SEPARATOR)
                if (parts.size != 3) return@mapNotNull null

                val playerUuid = parts[0]
                val questId = parts[1].toIntOrNull() ?: return@mapNotNull null
                val progress = parts[2].toIntOrNull() ?: return@mapNotNull null

                if (playerUuid != uuid) return@mapNotNull null
                QuestPDC(playerUuid = playerUuid, questId = questId, questProgress = progress)
            }
        }
        set(value) {
            val uuid = uniqueId.toString()
            val pdc = persistentDataContainer

            pdc.keys
                .asSequence()
                .filter { it.namespace == instance.name.lowercase() }
                .filter { it.key.startsWith(QUEST_KEY_PREFIX) }
                .toList()
                .forEach { pdc.remove(it) }

            if (value == null) return

            value
                .asSequence()
                .filter { it.playerUuid == uuid }
                .forEach { q ->
                    val key = NamespacedKey(instance, "$QUEST_KEY_PREFIX${q.questId}")
                    val encoded =
                        listOf(q.playerUuid, q.questId.toString(), q.questProgress.toString())
                            .joinToString(QUEST_VALUE_SEPARATOR)
                    pdc.set(key, PersistentDataType.STRING, encoded)
                }
        }
}
