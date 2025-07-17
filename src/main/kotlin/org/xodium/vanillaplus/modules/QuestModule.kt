package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.container.type.PaperContainerType.hopper
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.QuestData
import org.xodium.vanillaplus.enums.QuestDifficultyEnum
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.FmtUtils.skylineFmt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Represents a module handling quest mechanics within the system. */
class QuestModule : ModuleInterface<QuestModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    override fun cmds(): CommandData {
        return CommandData(
            listOf(
                Commands.literal("quests")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx -> ctx.tryCatch { gui().open(it.sender as Player) } }
            ),
            "Allows players to open the quests gui.",
            listOf("q")
        )
    }

    override fun perms(): List<Permission> {
        return listOf(
            Permission(
                "${instance::class.simpleName}.quests.gui.use".lowercase(),
                "Allows use of the quests command",
                PermissionDefault.TRUE
            )
        )
    }

    /**
     * Constructs and configures a GUI for the associated module.
     * @return A fully configured `Gui` instance with the specified properties and components.
     */
    private fun gui(): Gui {
        return buildGui {
            spamPreventionDuration = config.spamPreventionDuration
            containerType = hopper()
            title(config.guiTitle.mm())
            statelessComponent { container ->
                val easyQuests = config.quests[QuestDifficultyEnum.EASY].orEmpty().shuffled()
                val mediumQuests = config.quests[QuestDifficultyEnum.MEDIUM].orEmpty().shuffled()
                val hardQuests = config.quests[QuestDifficultyEnum.HARD].orEmpty().shuffled()

                fun questItem(quest: QuestData, difficulty: QuestDifficultyEnum): ItemStack {
                    val completed = quest.completed
                    val difficultyColor = when (difficulty) {
                        QuestDifficultyEnum.EASY -> "<green>"
                        QuestDifficultyEnum.MEDIUM -> "<yellow>"
                        QuestDifficultyEnum.HARD -> "<red>"
                    }
                    val name =
                        "<b>$difficultyColor${
                            difficulty.name.lowercase().replaceFirstChar { it.uppercase() }
                        } ${"Quest".skylineFmt()}"
                    val material = if (completed) Material.WRITABLE_BOOK else Material.ENCHANTED_BOOK
                    val lore = if (completed) listOf("Completed".mangoFmt()) else listOf(
                        "${"<b>Task ➛</b>".fireFmt()} ${quest.task.fireFmt(true)}",
                        "${"<b>Reward ➛</b>".mangoFmt()} ${quest.reward.mangoFmt(true)}"
                    )
                    return guiItemStack(material, name, lore)
                }

                if (easyQuests.size >= 2) {
                    container[0] = ItemBuilder.from(questItem(easyQuests[0], QuestDifficultyEnum.EASY)).asGuiItem()
                    container[1] = ItemBuilder.from(questItem(easyQuests[1], QuestDifficultyEnum.EASY)).asGuiItem()
                }

                if (mediumQuests.size >= 2) {
                    container[2] = ItemBuilder.from(questItem(mediumQuests[0], QuestDifficultyEnum.MEDIUM)).asGuiItem()
                    container[3] = ItemBuilder.from(questItem(mediumQuests[1], QuestDifficultyEnum.MEDIUM)).asGuiItem()
                }

                if (hardQuests.isNotEmpty()) {
                    container[4] = ItemBuilder.from(questItem(hardQuests[0], QuestDifficultyEnum.HARD)).asGuiItem()
                }
            }
        }
    }

    /**
     * Creates an instance of an ItemStack with the specified material, name, and lore,
     * and applies the desired display properties to it.
     * @param material The material of the ItemStack.
     * @param itemName The display name of the ItemStack, parsed as a MiniMessage component.
     * @param itemLore The lore of the ItemStack, parsed as a list of MiniMessage components.
     * @return An ItemStack configured with the provided material, name, and lore.
     */
    private fun guiItemStack(material: Material, itemName: String, itemLore: List<String>): ItemStack {
        @Suppress("UnstableApiUsage")
        return ItemStack.of(material).apply {
            setData(DataComponentTypes.ITEM_NAME, itemName.mm())
            setData(DataComponentTypes.LORE, ItemLore.lore(itemLore.mm()))
        }
    }

    data class Config(
        override var enabled: Boolean = true,
        var spamPreventionDuration: Duration = 1.seconds,
        var guiTitle: String = "<b>Quests</b>".fireFmt(),
        var quests: Map<QuestDifficultyEnum, List<QuestData>> = mapOf(
            Pair(
                QuestDifficultyEnum.EASY,
                listOf(
                    QuestData(task = "Collect 10 Oak Logs", reward = "5 XP Bottles"),
                    QuestData(task = "Mine 8 Coal Ore", reward = "Leather Armor Set"),
                    QuestData(task = "Kill 3 Zombies", reward = "Iron Sword"),
                    QuestData(task = "Craft a Stone Pickaxe", reward = "Golden Apple"),
                    QuestData(task = "Fish 2 Fish", reward = "5 Cooked Cod"),
                )
            ),
            Pair(
                QuestDifficultyEnum.MEDIUM,
                listOf(
                    QuestData(task = "Collect 20 Iron Ingots", reward = "Diamond Pickaxe"),
                    QuestData(task = "Kill 5 Skeletons", reward = "Enchanted Bow (Power I)"),
                    QuestData(task = "Breed 3 Animals", reward = "10 Golden Carrots"),
                    QuestData(task = "Craft an Enchanting Table", reward = "5 Lapis Lazuli Blocks"),
                    QuestData(task = "Travel 500 Blocks", reward = "Ender Pearl x3"),
                )
            ),
            Pair(
                QuestDifficultyEnum.HARD,
                listOf(
                    QuestData(task = "Kill an Enderman", reward = "Netherite Ingot"),
                    QuestData(task = "Collect 3 Blaze Rods", reward = "Totem of Undying"),
                    QuestData(
                        task = "Conquer a Pillager Outpost",
                        reward = "Enchanted Diamond Chestplate (Protection II)"
                    ),
                    QuestData(task = "Mine 5 Diamonds", reward = "Beacon"),
                    QuestData(task = "Brew a Splash Potion of Weakness", reward = "Villager Spawn Egg"),
                )
            ),
        ),
    ) : ModuleInterface.Config
}