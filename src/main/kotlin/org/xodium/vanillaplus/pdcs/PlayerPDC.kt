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
    private val QUEST_ID_KEY = NamespacedKey(instance, "quest_id")
    private val QUEST_PROGRESS_KEY = NamespacedKey(instance, "quest_progress")

    /**
     * Data class representing a player's quest data stored in their persistent data container.
     * @property questId The ID of the quest.
     * @property questProgress The progress made in the quest.
     */
    data class QuestPDC(
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
            val listType = PersistentDataType.LIST.listTypeFrom(PersistentDataType.TAG_CONTAINER)
            val containers = persistentDataContainer.get(QUESTS_KEY, listType) ?: return null
            val decoded =
                containers.mapNotNull { c ->
                    val questId = c.get(QUEST_ID_KEY, PersistentDataType.INTEGER) ?: return@mapNotNull null
                    val progress = c.get(QUEST_PROGRESS_KEY, PersistentDataType.INTEGER) ?: return@mapNotNull null

                    QuestPDC(questId = questId, questProgress = progress)
                }

            return decoded.ifEmpty { null }
        }
        set(value) {
            val pdc = persistentDataContainer

            if (value == null) {
                pdc.remove(QUESTS_KEY)
                return
            }

            val ctx = pdc.adapterContext
            val containers =
                value
                    .map { q ->
                        ctx.newPersistentDataContainer().apply {
                            set(QUEST_ID_KEY, PersistentDataType.INTEGER, q.questId)
                            set(QUEST_PROGRESS_KEY, PersistentDataType.INTEGER, q.questProgress)
                        }
                    }.toList()
            val listType = PersistentDataType.LIST.listTypeFrom(PersistentDataType.TAG_CONTAINER)

            pdc.set(QUESTS_KEY, listType, containers)
        }
}
