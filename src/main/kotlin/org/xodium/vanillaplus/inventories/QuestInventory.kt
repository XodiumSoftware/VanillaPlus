@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.inventories

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.QuestData
import org.xodium.vanillaplus.enums.QuestDifficultyEnum
import org.xodium.vanillaplus.enums.QuestTypeEnum
import org.xodium.vanillaplus.interfaces.InventoryInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import java.util.*

/** Represents the inventory for quest-related items and interactions. */
internal object QuestInventory : InventoryInterface {
    /** Map of quest difficulties to their respective quest pools. */
    private val questPool: Map<QuestDifficultyEnum, List<QuestData>> =
        QuestDifficultyEnum.entries.associateWith { difficulty ->
            when (difficulty) {
                QuestDifficultyEnum.EASY -> {
                    listOf(
                        QuestData(
                            objective = "Collect 5 wooden planks",
                            crystals = 5,
                            type = QuestTypeEnum.COLLECT_ITEM,
                            requiredAmount = 5,
                        ),
                        QuestData(
                            objective = "Mine 10 cobblestone",
                            crystals = 8,
                            type = QuestTypeEnum.MINE_BLOCK,
                            requiredAmount = 10,
                        ),
                        QuestData(
                            objective = "Craft a wooden pickaxe",
                            crystals = 10,
                            type = QuestTypeEnum.CRAFT_ITEM,
                            requiredAmount = 1,
                        ),
                        QuestData(
                            objective = "Collect 3 wheat",
                            crystals = 5,
                            type = QuestTypeEnum.COLLECT_ITEM,
                            requiredAmount = 3,
                        ),
                    )
                }

                QuestDifficultyEnum.MEDIUM -> {
                    listOf(
                        QuestData(
                            objective = "Craft a stone pickaxe",
                            crystals = 20,
                            type = QuestTypeEnum.CRAFT_ITEM,
                            requiredAmount = 1,
                        ),
                        QuestData(
                            objective = "Defeat 3 skeletons",
                            crystals = 25,
                            type = QuestTypeEnum.KILL_ENTITY,
                            requiredAmount = 3,
                        ),
                        QuestData(
                            objective = "Mine 5 iron ore",
                            crystals = 30,
                            type = QuestTypeEnum.MINE_BLOCK,
                            requiredAmount = 5,
                        ),
                        QuestData(
                            objective = "Cook 10 food items",
                            crystals = 22,
                            type = QuestTypeEnum.COOK_ITEM,
                            requiredAmount = 10,
                        ),
                    )
                }

                QuestDifficultyEnum.HARD -> {
                    listOf(
                        QuestData(
                            objective = "Slay 1 cave spider boss",
                            crystals = 100,
                            type = QuestTypeEnum.KILL_ENTITY,
                            requiredAmount = 1,
                        ),
                        QuestData(
                            objective = "Defeat 5 zombies without taking damage",
                            crystals = 80,
                            type = QuestTypeEnum.KILL_ENTITY,
                            requiredAmount = 5,
                        ),
                        QuestData(
                            objective = "Mine 10 diamonds",
                            crystals = 120,
                            type = QuestTypeEnum.MINE_BLOCK,
                            requiredAmount = 10,
                        ),
                        QuestData(
                            objective = "Complete 1 dungeon",
                            crystals = 150,
                            type = QuestTypeEnum.COLLECT_ITEM,
                            requiredAmount = 1,
                        ),
                    )
                }
            }
        }

    private val _inventory: Inventory =
        instance.server.createInventory(this, 9, "<gradient:#CB2D3E:#EF473A>Quests</gradient>".mm()).apply {
            fill(Material.GRAY_STAINED_GLASS_PANE)

            // EASY Quests (slots 2, 3)
            setItem(2, randomQuestItem(QuestDifficultyEnum.EASY))
            setItem(3, randomQuestItem(QuestDifficultyEnum.EASY))

            // MEDIUM Quests (slots 4, 5)
            setItem(4, randomQuestItem(QuestDifficultyEnum.MEDIUM))
            setItem(5, randomQuestItem(QuestDifficultyEnum.MEDIUM))

            // HARD Quest (slot 6)
            setItem(6, randomQuestItem(QuestDifficultyEnum.HARD))
        }

    override fun getInventory(): Inventory = _inventory

    /**
     * Creates a random quest item from the pool for the specified difficulty.
     * @param difficulty The difficulty level of the quest.
     * @param material The material of the quest item. Defaults to PAPER.
     * @return The created quest item as an ItemStack.
     */
    private fun randomQuestItem(
        difficulty: QuestDifficultyEnum,
        material: Material = Material.PAPER,
    ): ItemStack {
        val quest = questPool.getValue(difficulty).random()
        return questItem(difficulty, quest.objective, quest.crystals, quest.uuid, material)
    }

    /**
     * Creates a quest item with the specified title, objective, rewards, and material.
     * @param difficulty The difficulty level of the quest.
     * @param objective The objective description of the quest.
     * @param crystals The number of crystals rewarded for completing the quest.
     * @param uuid The unique identifier for the quest.
     * @param material The material of the quest item. Defaults to PAPER.
     * @return The created quest item as an ItemStack.
     */
    @Suppress("UnstableApiUsage")
    private fun questItem(
        difficulty: QuestDifficultyEnum,
        objective: String,
        crystals: Int,
        uuid: UUID,
        material: Material = Material.PAPER,
    ): ItemStack =
        ItemStack.of(material).apply {
            setData(DataComponentTypes.CUSTOM_NAME, difficulty.title)
            setData(
                DataComponentTypes.LORE,
                ItemLore.lore(
                    listOf(
                        Component.empty(),
                        "<yellow>> Objective:".mm(),
                        "<white>$objective".mm(),
                        Component.empty(),
                        "<yellow>> Rewards:".mm(),
                        "<white>$crystals Crystals".mm(),
                    ),
                ),
            )
            editPersistentDataContainer { container ->
                container.set(
                    NamespacedKey(instance, "quest_uuid"),
                    PersistentDataType.STRING,
                    uuid.toString(),
                )
            }
        }

    /**
     * Creates a crystal ItemStack with the specified amount.
     * @param amount The amount of crystals. Defaults to 1.
     * @return The created crystal ItemStack.
     */
    @Suppress("UnstableApiUsage")
    fun crystal(amount: Int = 1): ItemStack =
        ItemStack.of(Material.AMETHYST_SHARD, amount).apply {
            setData(DataComponentTypes.CUSTOM_NAME, "<gradient:#9b59b6:#8e44ad>Crystal</gradient>".mm())
            setData(
                DataComponentTypes.LORE,
                ItemLore.lore(
                    listOf(
                        Component.empty(),
                        "<yellow>A rare crystal used as currency,".mm(),
                        "<yellow>obtainable via quests.".mm(),
                    ),
                ),
            )
        }

    /**
     * Generates a set of weekly quest UUIDs (2 easy, 2 medium, 1 hard).
     * @return A set of quest UUIDs for the week.
     */
    fun generateWeeklyQuests(): Set<UUID> {
        val quests = mutableSetOf<UUID>()

        questPool
            .getValue(QuestDifficultyEnum.EASY)
            .shuffled()
            .take(2)
            .forEach { quests.add(it.uuid) }
        questPool
            .getValue(QuestDifficultyEnum.MEDIUM)
            .shuffled()
            .take(2)
            .forEach { quests.add(it.uuid) }
        questPool
            .getValue(QuestDifficultyEnum.HARD)
            .shuffled()
            .take(1)
            .forEach { quests.add(it.uuid) }

        return quests
    }

    /**
     * Gets a quest by its UUID.
     * @param uuid The UUID of the quest.
     * @return The QuestData if found, null otherwise.
     */
    fun getQuestByUuid(uuid: UUID): QuestData? = questPool.values.flatten().find { it.uuid == uuid }
}
