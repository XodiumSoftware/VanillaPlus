@file:OptIn(ExperimentalUuidApi::class)

package org.xodium.vanillaplus.databases

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.xodium.vanillaplus.modules.QuestModule
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/** Manages the quest database. */
internal class QuestDatabase(
    private val dbFile: File,
) {
    /**
     * Establishes and returns a connection to the SQLite database.
     * @return A [Connection] object representing the database connection.
     */
    private fun connect(): Connection {
        dbFile.parentFile?.mkdirs()
        return DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
    }

    init {
        connect().use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    """
                    PRAGMA journal_mode=WAL;
                    """.trimIndent(),
                )
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS quest_progress (
                      player_uuid TEXT NOT NULL,
                      slot INTEGER NOT NULL,
                      difficulty TEXT NOT NULL,
                      req_entity_type TEXT,
                      req_material TEXT,
                      target_amount INTEGER NOT NULL,
                      current_progress INTEGER NOT NULL,
                      reward_type TEXT NOT NULL,
                      reward_amount INTEGER NOT NULL,
                      PRIMARY KEY (player_uuid, slot)
                    );
                    """.trimIndent(),
                )
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS quest_all_reward_claimed (
                      player_uuid TEXT PRIMARY KEY
                    );
                    """.trimIndent(),
                )
            }
        }
    }

    /**
     * Loads the player's quests from the database.
     * @param playerId The UUID of the player.
     * @return A list of [QuestModule.Quest] objects representing the player's quests.
     */
    fun loadQuests(playerId: Uuid): List<QuestModule.Quest> {
        val out = ArrayList<QuestModule.Quest>()

        connect().use { connection ->
            connection
                .prepareStatement(
                    """
                    SELECT slot, difficulty, req_entity_type, req_material, target_amount, current_progress, reward_type, reward_amount
                    FROM quest_progress
                    WHERE player_uuid = ?
                    ORDER BY slot ASC;
                    """.trimIndent(),
                ).use { preparedStatement ->
                    preparedStatement.setString(1, playerId.toJavaUuid().toString())
                    preparedStatement.executeQuery().use { rs ->
                        while (rs.next()) {
                            val difficulty = QuestModule.Quest.Difficulty.valueOf(rs.getString("difficulty"))
                            val reqEntity = rs.getString("req_entity_type")?.let { EntityType.valueOf(it) }
                            val reqMaterial = rs.getString("req_material")?.let { Material.valueOf(it) }
                            val targetAmount = rs.getInt("target_amount")
                            val currentProgress = rs.getInt("current_progress")
                            val rewardType = Material.valueOf(rs.getString("reward_type"))
                            val rewardAmount = rs.getInt("reward_amount")
                            val req =
                                QuestModule.Quest.Requirement(reqEntity, reqMaterial, targetAmount).also {
                                    it.currentProgress = currentProgress
                                }
                            val reward = QuestModule.Quest.Reward(rewardType, rewardAmount)

                            out.add(QuestModule.Quest(difficulty, req, reward))
                        }
                    }
                }
        }
        return out
    }

    /**
     * Saves the player's quests to the database.
     * @param playerId The UUID of the player.
     * @param quests The list of quests to save.
     */
    fun saveQuests(
        playerId: Uuid,
        quests: List<QuestModule.Quest>,
    ) {
        connect().use { connection ->
            connection.autoCommit = false
            try {
                connection
                    .prepareStatement(
                        "DELETE FROM quest_progress WHERE player_uuid = ?;",
                    ).use { preparedStatement ->
                        preparedStatement.setString(1, playerId.toJavaUuid().toString())
                        preparedStatement.executeUpdate()
                    }

                connection
                    .prepareStatement(
                        """
                        INSERT INTO quest_progress (
                          player_uuid, slot, difficulty,
                          req_entity_type, req_material,
                          target_amount, current_progress,
                          reward_type, reward_amount
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
                        """.trimIndent(),
                    ).use { preparedStatement ->
                        quests.forEachIndexed { index, quest ->
                            preparedStatement.setString(1, playerId.toJavaUuid().toString())
                            preparedStatement.setInt(2, index)
                            preparedStatement.setString(3, quest.difficulty.name)
                            preparedStatement.setString(4, quest.requirement.entityType?.name)
                            preparedStatement.setString(5, quest.requirement.material?.name)
                            preparedStatement.setInt(6, quest.requirement.targetAmount)
                            preparedStatement.setInt(7, quest.requirement.currentProgress)
                            preparedStatement.setString(8, quest.reward.type.name)
                            preparedStatement.setInt(9, quest.reward.amount)
                            preparedStatement.addBatch()
                        }
                        preparedStatement.executeBatch()
                    }

                connection.commit()
            } catch (t: Throwable) {
                connection.rollback()
                throw t
            } finally {
                connection.autoCommit = true
            }
        }
    }

    /**
     * Checks if the player has claimed the "all quests" reward.
     * @param playerId The UUID of the player.
     * @return True if the reward has been claimed, false otherwise.
     */
    fun hasClaimedAllReward(playerId: Uuid): Boolean {
        connect().use { connection ->
            connection
                .prepareStatement(
                    "SELECT 1 FROM quest_all_reward_claimed WHERE player_uuid = ? LIMIT 1;",
                ).use { preparedStatement ->
                    preparedStatement.setString(1, playerId.toJavaUuid().toString())
                    preparedStatement.executeQuery().use { resultSet -> return resultSet.next() }
                }
        }
    }

    /**
     * Sets whether the player has claimed the "all quests" reward.
     * @param playerId The UUID of the player.
     * @param claimed True if the reward has been claimed, false otherwise.
     */
    fun setClaimedAllReward(
        playerId: Uuid,
        claimed: Boolean,
    ) {
        connect().use { connection ->
            if (claimed) {
                connection
                    .prepareStatement(
                        "INSERT OR IGNORE INTO quest_all_reward_claimed (player_uuid) VALUES (?);",
                    ).use { preparedStatement ->
                        preparedStatement.setString(1, playerId.toJavaUuid().toString())
                        preparedStatement.executeUpdate()
                    }
            } else {
                connection
                    .prepareStatement(
                        "DELETE FROM quest_all_reward_claimed WHERE player_uuid = ?;",
                    ).use { preparedStatement ->
                        preparedStatement.setString(1, playerId.toJavaUuid().toString())
                        preparedStatement.executeUpdate()
                    }
            }
        }
    }
}
