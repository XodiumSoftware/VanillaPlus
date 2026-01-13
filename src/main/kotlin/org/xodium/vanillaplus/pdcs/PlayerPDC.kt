package org.xodium.vanillaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.pdcs.PlayerPDC.ALL_QUESTS_COMPLETED_KEY
import org.xodium.vanillaplus.pdcs.PlayerPDC.NICKNAME_KEY
import org.xodium.vanillaplus.pdcs.PlayerPDC.QUESTS_KEY
import org.xodium.vanillaplus.pdcs.PlayerPDC.QUEST_ID_KEY
import org.xodium.vanillaplus.pdcs.PlayerPDC.QUEST_PROGRESS_KEY
import org.xodium.vanillaplus.pdcs.PlayerPDC.SCOREBOARD_VISIBILITY_KEY

/**
 * Provides access to player-specific persistent data including nicknames and scoreboard preferences.
 * @property NICKNAME_KEY The namespaced key used for storing nickname data.
 * @property SCOREBOARD_VISIBILITY_KEY The namespaced key used for storing scoreboard visibility preferences.
 * @property QUESTS_KEY The namespaced key used for storing quest data.
 * @property QUEST_ID_KEY The namespaced key used for storing quest IDs.
 * @property QUEST_PROGRESS_KEY The namespaced key used for storing quest progress.
 * @property ALL_QUESTS_COMPLETED_KEY The namespaced key used for storing whether all quests are completed.
 */
internal object PlayerPDC {
    private val NICKNAME_KEY = NamespacedKey(instance, "nickname")
    private val SCOREBOARD_VISIBILITY_KEY = NamespacedKey(instance, "scoreboard_visibility")
    private val QUESTS_KEY = NamespacedKey(instance, "quests")
    private val QUEST_ID_KEY = NamespacedKey(instance, "quest_id")
    private val QUEST_PROGRESS_KEY = NamespacedKey(instance, "quest_progress")
    private val ALL_QUESTS_COMPLETED_KEY = NamespacedKey(instance, "all_quests_completed")

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
     * @return A map of quest IDs to their progress, or null if no quests are stored.
     */
    var Player.quests: Map<Int, Int>?
        get() {
            val listType = PersistentDataType.LIST.listTypeFrom(PersistentDataType.TAG_CONTAINER)
            val containers = persistentDataContainer.get(QUESTS_KEY, listType) ?: return null

            return containers
                .mapNotNull { container ->
                    val id = container.get(QUEST_ID_KEY, PersistentDataType.INTEGER) ?: return@mapNotNull null
                    val progress =
                        container.get(QUEST_PROGRESS_KEY, PersistentDataType.INTEGER) ?: return@mapNotNull null
                    id to progress
                }.toMap()
                .ifEmpty { null }
        }
        set(value) {
            val pdc = persistentDataContainer

            if (value == null) {
                pdc.remove(QUESTS_KEY)
                return
            }

            val containers =
                value.map { (id, progress) ->
                    pdc.adapterContext.newPersistentDataContainer().apply {
                        set(QUEST_ID_KEY, PersistentDataType.INTEGER, id)
                        set(QUEST_PROGRESS_KEY, PersistentDataType.INTEGER, progress)
                    }
                }
            val listType = PersistentDataType.LIST.listTypeFrom(PersistentDataType.TAG_CONTAINER)

            pdc.set(QUESTS_KEY, listType, containers)
        }

    /**
     * Gets or sets whether the player has completed all quests in their persistent data container.
     * @receiver The player whose quest completion status to access.
     * @return True if all quests are completed, false otherwise.
     */
    var Player.allQuestsCompleted: Boolean
        get() = persistentDataContainer.get(ALL_QUESTS_COMPLETED_KEY, PersistentDataType.BOOLEAN) ?: false
        set(value) = persistentDataContainer.set(ALL_QUESTS_COMPLETED_KEY, PersistentDataType.BOOLEAN, value)
}
